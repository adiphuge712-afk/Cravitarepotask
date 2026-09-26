package com.example.demo.exception;

/**
 * A referenced record does not exist. Maps to HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param resource what was being looked up, e.g. "Coach"
	 * @param id       the identifier that did not resolve
	 */
	public ResourceNotFoundException(String resource, long id) {
		super(resource + " not found with id " + id);
	}
}
