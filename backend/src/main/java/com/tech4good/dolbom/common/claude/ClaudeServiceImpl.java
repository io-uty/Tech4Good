package com.tech4good.dolbom.common.claude;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClaudeServiceImpl implements ClaudeService {

	private static final String JSON_ONLY_RULE =
			"\n\n[출력 규칙] 반드시 요구된 JSON 스키마를 정확히 따르는 JSON만 출력하라. "
			+ "스키마 외의 설명 텍스트, 마크다운, 주석을 절대 포함하지 마라.";

	@Value("${anthropic.api-key}")
	private String apiKey;

	@Value("${claude.io-log-path}")
	private String ioLogPath;

	private volatile AnthropicClient client;

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
				log.warn("Claude structured call failed (attempt {}/{}): {}", attempt + 1, maxRetries + 1, e.getMessage());
				// 오류 메시지를 포함해 재호출 — 임의값으로 채워 반환하지 않는다 (CLAUDE.md 5절)
				prompt = userPrompt + "\n\n[이전 시도 실패 원인] " + e.getMessage()
						+ "\n위 오류를 참고해 스키마에 맞는 JSON만 다시 출력하라.";
			}
		}
		throw new ClaudeSchemaValidationException(
				"Claude 응답이 " + (maxRetries + 1) + "회 시도 후에도 스키마 검증을 통과하지 못했습니다.", lastError);
	}

	private <T> T callOnce(String systemPrompt, String userPrompt, Class<T> responseType) {
		StructuredMessageCreateParams<T> params = MessageCreateParams.builder()
				.model("claude-opus-4-8")
				.maxTokens(16000L)
				.system(systemPrompt + JSON_ONLY_RULE)
				.outputConfig(responseType)
				.addUserMessage(userPrompt)
				.build();

		var response = client().messages().create(params);
		logIo(systemPrompt, userPrompt, String.valueOf(response));

		return response.content().stream()
				.flatMap(block -> block.text().stream())
				.findFirst()
				.map(textBlock -> textBlock.text())
				.orElseThrow(() -> new IllegalStateException(
						"Claude 응답에 텍스트 블록이 없습니다 (stop_reason=" + response.stopReason() + ")"));
	}

	private AnthropicClient client() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					if (apiKey == null || apiKey.isBlank()) {
						throw new IllegalStateException(
								"ANTHROPIC_API_KEY가 설정되지 않았습니다. backend/.env 파일을 채워주세요.");
					}
					client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
				}
			}
		}
		return client;
	}

	/** 요청/응답 원문을 파일 로그로 남긴다 (CLAUDE.md 5절 디버깅 요구사항). */
	private void logIo(String systemPrompt, String userPrompt, String rawResponse) {
		try {
			Path path = Path.of(ioLogPath);
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}
			String entry = "===== " + Instant.now() + " =====\n"
					+ "[SYSTEM]\n" + systemPrompt + "\n"
					+ "[USER]\n" + userPrompt + "\n"
					+ "[RESPONSE]\n" + rawResponse + "\n\n";
			Files.writeString(path, entry, StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException e) {
			log.warn("Claude IO 로그 기록 실패: {}", e.getMessage());
		}
	}
}
