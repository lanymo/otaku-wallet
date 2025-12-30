package com.otaku.wallet.exception;

/**
 * 잘못된 지출 데이터가 입력되었을 때 발생하는 예외
 */
public class InvalidExpenseDataException extends RuntimeException {

    public InvalidExpenseDataException(String message) {
        super(message);
    }

    public InvalidExpenseDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
