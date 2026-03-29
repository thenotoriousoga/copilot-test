package com.example.api.exception;

/**
 * セキュリティ違反が検出された場合にスローされる非検査例外
 */
public class SecurityViolationException extends RuntimeException {

    public SecurityViolationException(String message) {
        super(message);
    }
}
