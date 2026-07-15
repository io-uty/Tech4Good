package com.tech4good.dolbom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech4good.dolbom.common.claude.AnthropicClaudeService;
import com.tech4good.dolbom.common.claude.ClaudeService;
import com.tech4good.dolbom.common.claude.GeminiClaudeService;

import lombok.extern.slf4j.Slf4j;

/**
 * LLM provider 선택:
 * - ANTHROPIC_API_KEY가 있으면 Claude (기본, CLAUDE.md 기술 스택)
 * - 없고 GOOGLE_API_KEY가 있으면 Gemini (해커톤 제공 키 대응)
 */
@Slf4j
@Configuration
public class LlmConfig {

	@Bean
	public ClaudeService claudeService(
			@Value("${anthropic.api-key}") String anthropicKey,
			@Value("${google.api-key}") String googleKey,
			@Value("${gemini.model}") String geminiModel,
			@Value("${claude.io-log-path}") String ioLogPath,
			ObjectMapper firestoreObjectMapper) {
		if (anthropicKey != null && !anthropicKey.isBlank()) {
			log.info("LLM provider: Claude (claude-opus-4-8)");
			return new AnthropicClaudeService(anthropicKey, ioLogPath);
		}
		if (googleKey != null && !googleKey.isBlank()) {
			log.info("LLM provider: Gemini ({})", geminiModel);
			return new GeminiClaudeService(googleKey, geminiModel, ioLogPath, firestoreObjectMapper);
		}
		// 키가 없어도 앱은 뜨게 하되, 호출 시점에 명확한 오류
		log.warn("ANTHROPIC_API_KEY / GOOGLE_API_KEY 둘 다 없음 — LLM 호출 시 오류가 발생합니다.");
		return new AbstractFailingClaudeService();
	}

	private static class AbstractFailingClaudeService implements ClaudeService {
		@Override
		public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
			return generateStructured(systemPrompt, userPrompt, responseType, 0);
		}

		@Override
		public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType, int maxRetries) {
			throw new IllegalStateException(
					"LLM API 키가 없습니다. backend/.env에 ANTHROPIC_API_KEY 또는 GOOGLE_API_KEY를 설정해주세요.");
		}
	}
}
