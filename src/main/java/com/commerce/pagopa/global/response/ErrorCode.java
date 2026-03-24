package com.commerce.pagopa.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // COMMON
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,"COMMON_002", "허용되지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"COMMON_003", "서버 오류가 발생했습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "COMMON_004", "잘못된 타입입니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"AUTH_001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,"AUTH_002", "접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"AUTH_003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"AUTH_004", "만료된 토큰입니다."),

    // USER
    USER_NOT_FOUND(HttpStatus.NOT_FOUND,"USER_001", "존재하지 않는 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT,"USER_002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT,"USER_003", "이미 사용 중인 닉네임입니다."),

    // PRODUCT
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND,"PRODUCT_001", "존재하지 않는 상품입니다."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT,"PRODUCT_002", "재고가 부족합니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST,"PRODUCT_003", "판매 중인 상품이 아닙니다."),
    NOT_PRODUCT_OWNER(HttpStatus.FORBIDDEN,"PRODUCT_004", "상품 수정 권한이 없습니다."),

    // CATEGORY
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND,"CATEGORY_001", "존재하지 않는 카테고리입니다."),

    // CART
    CART_NOT_FOUND(HttpStatus.NOT_FOUND,"CART_001", "존재하지 않는 장바구니 항목입니다."),
    CART_ITEM_NOT_MINE(HttpStatus.FORBIDDEN,"CART_002", "본인의 장바구니 항목이 아닙니다."),

    // ORDER
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,"ORDER_001", "존재하지 않는 주문입니다."),
    ORDER_NOT_MINE(HttpStatus.FORBIDDEN,"ORDER_002", "본인의 주문이 아닙니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST,"ORDER_003", "취소할 수 없는 주문 상태입니다."),

    // REVIEW
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND,"REVIEW_001", "존재하지 않는 리뷰입니다."),
    REVIEW_NOT_MINE(HttpStatus.FORBIDDEN,"REVIEW_002", "본인의 리뷰가 아닙니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT,"REVIEW_003", "이미 리뷰를 작성했습니다."),
    REVIEW_NOT_PURCHASED(HttpStatus.FORBIDDEN,"REVIEW_004", "구매한 상품에만 리뷰를 작성할 수 있습니다."),

    // SCRAP
    SCRAP_NOT_FOUND(HttpStatus.NOT_FOUND,"SCRAP_001", "존재하지 않는 스크랩입니다."),
    SCRAP_ALREADY_EXISTS(HttpStatus.CONFLICT,"SCRAP_002", "이미 스크랩한 항목입니다.");

    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
