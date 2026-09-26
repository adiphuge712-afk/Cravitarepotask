package com.example.demo.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.example.demo.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes an {@link ErrorResponse} straight to the servlet response.
 *
 * <p>Needed because authentication and authorization failures happen inside
 * the filter chain, before any {@code @RestControllerAdvice} runs - without
 * this they come back as an empty body with only a status line.
 *
 * <p>The application-wide {@code ObjectMapper} is injected rather than
 * constructed, so these bodies serialize exactly like every other response.
 */
@Component
public class SecurityErrorWriter {

	private final ObjectMapper mapper;

	public SecurityErrorWriter(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
			throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ErrorResponse body = new ErrorResponse(
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI());

		mapper.writeValue(response.getWriter(), body);
	}
}
