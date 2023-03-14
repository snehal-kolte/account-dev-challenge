package com.dws.challenge.exception;

public class AmountTransactionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AmountTransactionException(String message) {
		super(message);
    }
}
