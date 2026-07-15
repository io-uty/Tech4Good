package com.tech4good.dolbom.common.claude;

/** 재시도 후에도 스키마에 맞는 Claude 응답을 얻지 못했을 때 던지는 예외 (CLAUDE.md 5절). */
public class ClaudeSchemaValidationException extends RuntimeException {

	public ClaudeSchemaValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
