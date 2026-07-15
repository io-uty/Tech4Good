package com.tech4good.dolbom.common;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tech4good.dolbom.common.claude.ClaudeSchemaValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiExceptions.NotFoundException.class)
	public ResponseEntity<Map<String, String>> handleNotFound(ApiExceptions.NotFoundException e) {
		return error(HttpStatus.NOT_FOUND, e.getMessage());
	}

	@ExceptionHandler(ApiExceptions.BadRequestException.class)
	public ResponseEntity<Map<String, String>> handleBadRequest(ApiExceptions.BadRequestException e) {
		return error(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(ClaudeSchemaValidationException.class)
	public ResponseEntity<Map<String, String>> handleClaudeValidation(ClaudeSchemaValidationException e) {
		log.error("Claude schema validation failed", e);
		return error(HttpStatus.BAD_GATEWAY, e.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException e) {
		log.error("Illegal state", e);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(Map.of("error", message == null ? "unknown" : message));
	}
}
