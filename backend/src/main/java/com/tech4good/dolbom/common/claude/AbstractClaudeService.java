package com.tech4good.dolbom.common.claude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClaudeService 공통 골격 — 재시도 루프 + 요청/응답 파일 로깅 (CLAUDE.md 5절).
 * 실제 LLM 호출은 provider별 구현체(Anthropic/Gemini)가 담당한다.
 */
public abstract class AbstractClaudeService implements ClaudeService {

	protected static final String JSON_ONLY_RULE =
			"\n\n[출력 규칙] 반드시 요구된 JSON 스키마를 정확히 따르는 JSON만 출력하라. "
			+ "스키마 외의 설명 텍스트, 마크다운, 주석을 절대 포함하지 마라.";

	private static final Logger log = LoggerFactory.getLogger(AbstractClaudeService.class);

	private final String ioLogPath;

	protected AbstractClaudeService(String ioLogPath) {
		this.ioLogPath = ioLogPath;
	}

	@Override
	public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType) {
		return generateStructured(systemPrompt, userPrompt, responseType, 2);
	}

	@Override
	public <T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType, int maxRetries) {
		String prompt = userPrompt;
		RuntimeException lastError = null;
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			try {
				return callOnce(systemPrompt, prompt, responseType);
			} catch (RuntimeException e) {
				lastError = e;
				log.warn("LLM structured call failed (attempt {}/{}): {}", attempt + 1, maxRetries + 1, e.getMessage());
				// 오류 메시지를 포함해 재호출 — 임의값으로 채워 반환하지 않는다 (CLAUDE.md 5절)
				prompt = userPrompt + "\n\n[이전 시도 실패 원인] " + e.getMessage()
						+ "\n위 오류를 참고해 스키마에 맞는 JSON만 다시 출력하라.";
			}
		}
		throw new ClaudeSchemaValidationException(
				"LLM 응답이 " + (maxRetries + 1) + "회 시도 후에도 스키마 검증을 통과하지 못했습니다.", lastError);
	}

	/** provider별 단일 호출. 스키마에 맞지 않으면 RuntimeException을 던져 재시도를 유도한다. */
	protected abstract <T> T callOnce(String systemPrompt, String userPrompt, Class<T> responseType);

	/** 요청/응답 원문을 파일 로그로 남긴다 (CLAUDE.md 5절 디버깅 요구사항). */
	protected void logIo(String systemPrompt, String userPrompt, String rawResponse) {
		try {
			Path path = Path.of(ioLogPath);
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}
			String entry = "===== " + Instant.now() + " [" + getClass().getSimpleName() + "] =====\n"
					+ "[SYSTEM]\n" + systemPrompt + "\n"
					+ "[USER]\n" + userPrompt + "\n"
					+ "[RESPONSE]\n" + rawResponse + "\n\n";
			Files.writeString(path, entry, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.warn("LLM IO 로그 기록 실패: {}", e.getMessage());
		}
	}
}
