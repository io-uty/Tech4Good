package com.tech4good.dolbom.common.claude;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;

/** Claude API 구현체 — Anthropic Structured Outputs로 응답 스키마를 서버 측에서 강제한다. */
public class AnthropicClaudeService extends AbstractClaudeService {

	private final String apiKey;
	private volatile AnthropicClient client;

	public AnthropicClaudeService(String apiKey, String ioLogPath) {
		super(ioLogPath);
		this.apiKey = apiKey;
	}

	@Override
	protected <T> T callOnce(String systemPrompt, String userPrompt, Class<T> responseType) {
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
					client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
				}
			}
		}
		return client;
	}
}
