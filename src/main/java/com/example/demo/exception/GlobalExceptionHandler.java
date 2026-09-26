package com.example.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.demo.dto.ErrorResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Turns every exception into one consistent {@link ErrorResponse} with an
 * HTTP status that actually describes what went wrong.
 *
 * <p>This replaces the per-endpoint {@code try/catch (Exception e)} blocks
 * that previously flattened a missing record, a duplicate email and a
 * malformed body all into the same response - and sometimes into a
 * <em>success</em> status, e.g. {@code 202 ACCEPTED "Registration fail"}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
		ErrorResponse body = new ErrorResponse(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI());
		return ResponseEntity.status(status).body(body);
	}

	/** A referenced record does not exist. */
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
	}

	/** Email already registered, or another uniqueness rule broken. */
	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), request);
	}

	/** Login failed. Deliberately does not say whether it was the email or the password. */
	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(InvalidCredentialsException ex,
			HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
	}

	/** Request contents are not acceptable. */
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
	}

	/** Bean-validation failures on an @Valid request body. */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + " " + e.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b)
				.orElse("Request validation failed");
		return build(HttpStatus.BAD_REQUEST, message, request);
	}

	/** Malformed or unparseable JSON body. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Malformed or missing request body", request);
	}

	/** A path variable or query parameter of the wrong type, e.g. /viewDataCoach/abc. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'", request);
	}

	/** A required query parameter was not supplied. */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "Missing required parameter '" + ex.getParameterName() + "'", request);
	}

	/** Authenticated, but not allowed to perform this action. */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", request);
	}

	/** Token present but expired - the frontend uses this to send the user back to login. */
	@ExceptionHandler(ExpiredJwtException.class)
	public ResponseEntity<ErrorResponse> handleExpiredToken(ExpiredJwtException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Session expired, please log in again", request);
	}

	/** Token malformed, tampered with, or signed by someone else. */
	@ExceptionHandler(JwtException.class)
	public ResponseEntity<ErrorResponse> handleInvalidToken(JwtException ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Invalid authentication token", request);
	}

	/**
	 * Spring Security rejected the credentials inside a controller - in practice
	 * this is {@code authenticate()} failing during login. Anonymous requests to
	 * a protected endpoint never reach here; they are answered by
	 * {@code RestAuthenticationEntryPoint} in the filter chain instead.
	 *
	 * <p>Uses the same wording as {@link InvalidCredentialsException} so that a
	 * wrong password and an unknown email remain indistinguishable.
	 */
	@ExceptionHandler({ AuthenticationException.class, UsernameNotFoundException.class })
	public ResponseEntity<ErrorResponse> handleAuthentication(Exception ex, HttpServletRequest request) {
		return build(HttpStatus.UNAUTHORIZED, "Invalid email, password or role", request);
	}

	/** A database constraint rejected the write - most often a duplicate email. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
			HttpServletRequest request) {
		log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
		return build(HttpStatus.CONFLICT, "That record already exists or violates a database constraint", request);
	}

	/**
	 * The mail server rejected or could not be reached. Distinct from a 500 so
	 * the user is told the code could not be delivered rather than being left
	 * waiting for an email that will never arrive.
	 */
	@ExceptionHandler(MailException.class)
	public ResponseEntity<ErrorResponse> handleMailFailure(MailException ex, HttpServletRequest request) {
		log.error("Could not send mail for {}", request.getRequestURI(), ex);
		return build(HttpStatus.BAD_GATEWAY,
				"We could not send the email just now. Please try again in a moment.", request);
	}

	/** No endpoint matches the requested path. */
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "No endpoint for " + request.getRequestURI(), request);
	}

	/**
	 * Anything unforeseen. The real cause is logged server-side; the client
	 * gets a generic message so stack traces and SQL never leak outward.
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception on {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
	}
}
