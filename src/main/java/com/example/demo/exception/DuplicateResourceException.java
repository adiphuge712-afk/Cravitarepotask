package com.example.demo.exception;

/**
 * A record already exists that would violate a uniqueness rule, most often a
 * registration using an email that is already taken. Maps to HTTP 409.
 */
public class DuplicateResourceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DuplicateResourceException(String message) {
		super(message);
	}

	public DuplicateResourceException(String message, Throwable cause) {
		super(message, cause);
	}
}
