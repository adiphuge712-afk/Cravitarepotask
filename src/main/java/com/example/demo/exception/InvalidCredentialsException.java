package com.example.demo.exception;

/**
 * The supplied email/password/role combination did not authenticate.
 * Maps to HTTP 401.
 *
 * <p>The message is deliberately the same whether the email was unknown or
 * the password was wrong, so the response cannot be used to enumerate which
 * accounts exist.
 */
public class InvalidCredentialsException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidCredentialsException() {
		super("Invalid email, password or role");
	}

	public InvalidCredentialsException(String message) {
		super(message);
	}
}
