package com.finflow.finflowbackend.common.exception;

public class DuplicateTransactionException extends RuntimeException{
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
