package com.commerce.pagopa.domain.payment.service;

import com.commerce.pagopa.domain.order.entity.Order;
import com.commerce.pagopa.domain.order.entity.enums.OrderStatus;
import com.commerce.pagopa.domain.order.repository.OrderRepository;
import com.commerce.pagopa.domain.payment.PaymentProperties;
import com.commerce.pagopa.domain.payment.dto.request.PaymentApproveRequestDto;
import com.commerce.pagopa.domain.payment.dto.response.PaymentResponseDto;
import com.commerce.pagopa.domain.payment.entity.Payment;
import com.commerce.pagopa.domain.payment.repository.PaymentRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.OrderNotFoundException;
import com.commerce.pagopa.global.exception.PaymentCancelException;
import com.commerce.pagopa.global.exception.PaymentNotFoundException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentProperties paymentProperties;
    private final RestClient restClient;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository,
            PaymentProperties paymentProperties
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentProperties = paymentProperties;

        // RestClient 초기화 시 공통 헤더(Authorization) 설정
        String encodedAuth = Base64.getEncoder()
                .encodeToString(
                        (paymentProperties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8)
                );
        this.restClient = RestClient.builder()
                .baseUrl(paymentProperties.getBaseUrl())
                .defaultHeader("Authorization", "Basic " + encodedAuth)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 주문 생성 직후, 결제 데이터를 준비하고 클라이언트(프론트엔드)가 토스 창을 띄울 수 있도록 DTO 반환
     */
    @Transactional
    public PaymentResponseDto requestPayment(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);

        // 결제 엔티티 생성 (상태: READY)
        Payment payment = Payment.create(order);
        paymentRepository.save(payment);
        
        payment.setInProgress();

        // 프론트엔드가 토스페이먼츠 SDK에 넘길 데이터를 반환
        return PaymentResponseDto.of(
                order.getOrderNumber(),
                order.getOrderName(),
                paymentProperties.getSuccessUrl(),
                paymentProperties.getFailUrl(),
                order.getTotalAmount(),
                order.getDelivery().getRecipientName()
        );
    }

    /**
     * 토스 결제 창에서 승인 후 Redirect로 돌아왔을 때, 토스 서버로 최종 결제 승인 API 호출
     */
    @Transactional
    public void confirmPayment(PaymentApproveRequestDto requestDto) {
        // orderId (orderNumber)로 주문 조회
        Order order = orderRepository.findByOrderNumber(requestDto.orderId())
                .orElseThrow(OrderNotFoundException::new);

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(PaymentNotFoundException::new);

        // 금액이 일치하는지 검증 (위변조 방지)
        if (payment.getAmount().compareTo(requestDto.amount()) != 0) {
            payment.fail();
            order.updateStatus(OrderStatus.CANCELLED);
            throw new PaymentCancelException();
        }

        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("orderId", requestDto.orderId());
            payload.put("amount", requestDto.amount().toString());
            payload.put("paymentKey", requestDto.paymentKey());

            ResponseEntity<String> response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .body(payload)
                    .retrieve()
                    .toEntity(String.class);

            // 성공 시 상태 업데이트
            if (response.getStatusCode().is2xxSuccessful()) {
                payment.success(requestDto.paymentKey());
                order.updateStatus(OrderStatus.PAID);
                log.info("결제 승인 성공: OrderNumber={}, PaymentKey={}", requestDto.orderId(), requestDto.paymentKey());
            } else {
                payment.fail();
                order.updateStatus(OrderStatus.CANCELLED);
                log.error("결제 승인 실패 응답: {}", response.getBody());
                throw new BusinessException(ErrorCode.PAYMENT_CONFIRM_FAIL);
            }
        } catch (Exception e) {
            payment.fail();
            order.updateStatus(OrderStatus.CANCELLED);
            log.error("토스 페이먼츠 API 호출 중 예외 발생", e);
            throw new BusinessException(ErrorCode.PAYMENT_REQUEST_ERROR);
        }
    }
}
