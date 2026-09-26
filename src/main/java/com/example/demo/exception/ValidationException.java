package com.example.demo.exception;

/**
 * The request was understood but its contents are not acceptable, e.g. a
 * registration with no email. Maps to HTTP 400.
 */
public class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
		super(message);
	}
}
