package com.commerce.pagopa.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, null, ApiError.of(errorCode, message));
    }

    @Getter
    @AllArgsConstructor
    static class ApiError {

        private final String code;
        private final String message;

        static ApiError of(ErrorCode errorCode) {
            return new ApiError(errorCode.getCode(), errorCode.getMessage());
        }

        static ApiError of(ErrorCode errorCode, String message) {
            return new ApiError(errorCode.getCode(), message);
        }
    }
}
