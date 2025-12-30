package com.otaku.wallet.exception;

/**
 * 지출 데이터를 찾을 수 없을 때 발생하는 예외
 */
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long id) {
        super("지출 내역을 찾을 수 없습니다. ID: " + id);
    }

    public ExpenseNotFoundException(String message) {
        super(message);
    }
}
