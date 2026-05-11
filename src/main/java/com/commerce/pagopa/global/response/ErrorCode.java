package com.commerce.pagopa.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_004", "잘못된 타입입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "COMMON_005", "접근이 거부되었습니다."),

    // IMAGE
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_001", "이미지 업로드에 실패했습니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_005", "리프레쉬 토큰을 찾지 못했습니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 닉네임입니다."),
    USER_NOT_ACTIVE(HttpStatus.CONFLICT, "USER_004", "활성화된 사용자가 아닙니다."),
    USER_NOT_BANNED(HttpStatus.CONFLICT, "USER_005", "정지된 사용자가 아닙니다."),

    // PRODUCT
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "존재하지 않는 상품입니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "PRODUCT_002", "재고가 부족합니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "판매 중인 상품이 아닙니다."),
    NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN, "PRODUCT_004", "상품 수정 권한이 없습니다."),
    BAD_REQUEST_QUANTITY(HttpStatus.BAD_REQUEST, "PRODUCT_005", "quantity는 1 이상이어야 합니다."),

    // CATEGORY
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_001", "존재하지 않는 카테고리입니다."),
    INVALID_CATEGORY_LEVEL(HttpStatus.BAD_REQUEST, "CATEGORY_002", "마지막 카테고리만 상품 등록이 가능합니다."),
    INVALID_CATEGORY_LEVEL_REQUEST(HttpStatus.BAD_REQUEST, "CATEGORY_003", "마지막 카테고리는 하위 카테고리를 가질 수 없습니다."),

    // CART
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "존재하지 않는 장바구니 항목입니다."),
    CART_ITEM_NOT_MINE(HttpStatus.FORBIDDEN, "CART_002", "본인의 장바구니 항목이 아닙니다."),
    CART_QUANTITY(HttpStatus.BAD_REQUEST, "CART_003", "수량은 0 이하로 감소할 수 없습니다."),

    // ORDER
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "존재하지 않는 주문입니다."),
    ORDER_NOT_MINE(HttpStatus.FORBIDDEN, "ORDER_002", "본인의 주문이 아닙니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "ORDER_003", "취소할 수 없는 주문 상태입니다."),
    ORDER_CANNOT_PAY(HttpStatus.BAD_REQUEST, "ORDER_004", "결제를 진행할 수 없는 주문 상태입니다."),
    ORDER_CANNOT_DELIVER(HttpStatus.BAD_REQUEST, "ORDER_005", "배송을 진행할 수 없는 주문 상태입니다."),
    ORDER_CANNOT_COMPLETE(HttpStatus.BAD_REQUEST, "ORDER_006", "배송 완료를 진행할 수 없는 주문 상태입니다."),

    // ORDER PRODUCT
    ORDER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_PRODUCT_001", "존재하지 않는 주문 상품입니다."),

    // SELLER ORDER (판매자별 출고 단위)
    SELLER_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER_ORDER_001", "존재하지 않는 판매자 주문입니다."),
    SELLER_ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "SELLER_ORDER_002", "취소할 수 없는 판매자 주문 상태입니다."),
    SELLER_ORDER_CANNOT_DELIVER(HttpStatus.BAD_REQUEST, "SELLER_ORDER_003", "발송 처리할 수 없는 판매자 주문 상태입니다."),
    SELLER_ORDER_CANNOT_COMPLETE(HttpStatus.BAD_REQUEST, "SELLER_ORDER_004", "배송 완료 처리할 수 없는 판매자 주문 상태입니다."),
    SELLER_ORDER_CANNOT_PAY(HttpStatus.BAD_REQUEST, "SELLER_ORDER_005", "결제 승인할 수 없는 판매자 주문 상태입니다."),
    SELLER_ORDER_NOT_FOR_THIS_SELLER(HttpStatus.NOT_FOUND, "SELLER_ORDER_006", "해당 판매자의 주문을 찾을 수 없습니다."),
    SELLER_ORDER_NOT_PROCESS(HttpStatus.BAD_REQUEST, "SELLER_ORDER_007", "처리할 수 없는 주문 상태입니다."),

    // REVIEW
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "존재하지 않는 리뷰입니다."),
    REVIEW_NOT_MINE(HttpStatus.FORBIDDEN, "REVIEW_002", "본인의 리뷰가 아닙니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "REVIEW_003", "이미 리뷰를 작성했습니다."),
    REVIEW_NOT_PURCHASED(HttpStatus.FORBIDDEN, "REVIEW_004", "구매한 상품만 리뷰를 작성할 수 있습니다."),

    // SCRAP
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND, "SCRAP_001", "존재하지 않는 스크랩입니다."),
    SCRAP_ALREADY_EXISTS(HttpStatus.CONFLICT, "SCRAP_002", "이미 스크랩한 항목입니다."),
    SCRAP_TARGET_UNSUPPORTED(HttpStatus.BAD_REQUEST, "SCRAP_003", "지원하지 않는 스크랩 대상 타입입니다."),

    // PAYMENT
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "해당 주문의 결제 정보가 존재하지 않습니다."),
    PAYMENT_CANCEL(HttpStatus.BAD_REQUEST, "PAYMENT_002", "결제를 취소합니다."),
    PAYMENT_CONFIRM_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_003", "결제 확인에 실패했습니다."),
    PAYMENT_REQUEST_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "PAYMENT_004", "결제 요청 중 오류가 발생했습니다."),
    PAYMENT_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "PAYMENT_005", "이미 처리 완료된 결제 건입니다."),
    PAYMENT_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "PAYMENT_006", "결제 승인 가능한 상태가 아닙니다."),
    PAYMENT_CANCEL_FAIL(HttpStatus.BAD_REQUEST, "PAYMENT_007", "결제 취소 요청을 실패했습니다."),
    PAYMENT_ALREADY_FAILED(HttpStatus.BAD_REQUEST, "PAYMENT_008", "이미 실패한 결제 건입니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "PAYMENT_009", "이미 취소된 결제 건입니다."),
    PAYMENT_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "PAYMENT_010", "결제를 취소할 수 없습니다."),
    PAYMENT_CANCEL_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_011", "취소 금액이 잘못되었습니다."),
    PAYMENT_CONFIRM_REJECTED(HttpStatus.BAD_REQUEST, "PAYMENT_012", "결제 승인이 거절되었습니다."),
    PAYMENT_CANCEL_REJECTED(HttpStatus.BAD_REQUEST, "PAYMENT_013", "결제 취소가 거절되었습니다."),

    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
