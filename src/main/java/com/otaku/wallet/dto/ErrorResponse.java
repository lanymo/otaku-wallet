package com.otaku.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 에러 응답 DTO
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

    /**
     * HTTP 상태 코드
     */
    private final int status;

    /**
     * 에러 메시지
     */
    private final String message;

    /**
     * 에러 발생 시각
     */
    private final LocalDateTime timestamp;

    /**
     * 요청 경로
     */
    private final String path;

    public static ErrorResponse of(int status, String message, String path) {
        return new ErrorResponse(status, message, LocalDateTime.now(), path);
    }
}
