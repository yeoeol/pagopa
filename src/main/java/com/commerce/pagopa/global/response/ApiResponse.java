package com.commerce.pagopa.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiError error;

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, ApiError.of(errorCode));
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, Object message) {
        return new ApiResponse<>(false, null, ApiError.of(errorCode, message));
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    static class ApiError {

        private final String code;
        private final Object message;

        static ApiError of(ErrorCode errorCode) {
            return new ApiError(errorCode.getCode(), errorCode.getMessage());
        }

        static ApiError of(ErrorCode errorCode, Object message) {
            return new ApiError(errorCode.getCode(), message);
        }
    }
}
