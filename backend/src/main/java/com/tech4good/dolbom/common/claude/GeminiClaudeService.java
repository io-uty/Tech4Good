package com.tech4good.dolbom.common.claude;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

/**
 * Gemini API 구현체 — ANTHROPIC_API_KEY가 없고 GOOGLE_API_KEY만 있을 때 사용.
 * 응답 클래스에서 JSON 스키마를 생성해 프롬프트에 포함하고, JSON 모드로 호출한 뒤
 * Jackson 역직렬화로 스키마를 검증한다 (실패 시 상위 재시도 루프가 동작).
 */
public class GeminiClaudeService extends AbstractClaudeService {

	private final String apiKey;
	private final String model;
	private final ObjectMapper objectMapper;
	private final SchemaGenerator schemaGenerator;
	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(20))
			.build();

	public GeminiClaudeService(String apiKey, String model, String ioLogPath, ObjectMapper objectMapper) {
		super(ioLogPath);
		this.apiKey = apiKey;
		this.model = model;
		this.objectMapper = objectMapper;
		this.schemaGenerator = new SchemaGenerator(
				new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
						.with(new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED))
						.build());
	}

	@Override
	protected <T> T callOnce(String systemPrompt, String userPrompt, Class<T> responseType) {
		String schema = schemaGenerator.generateSchema(responseType).toString();
		String system = systemPrompt + JSON_ONLY_RULE
				+ "\n\n[응답 JSON 스키마]\n" + schema;

		ObjectNode body = objectMapper.createObjectNode();
		body.putObject("systemInstruction").putArray("parts").addObject().put("text", system);
		body.putArray("contents").addObject()
				.put("role", "user")
				.putArray("parts").addObject().put("text", userPrompt);
		body.putObject("generationConfig").put("responseMimeType", "application/json");

		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(endpoint()))
					.timeout(Duration.ofMinutes(3))
					.header("Content-Type", "application/json")
					.header("x-goog-api-key", apiKey)
					.POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
					.build();

			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			logIo(system, userPrompt, response.body());

			if (response.statusCode() != 200) {
				throw new IllegalStateException("Gemini API 오류 (HTTP " + response.statusCode() + "): "
						+ truncate(response.body()));
			}

			JsonNode root = objectMapper.readTree(response.body());
			JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
			if (textNode.isMissingNode() || textNode.asText().isBlank()) {
				throw new IllegalStateException("Gemini 응답에 텍스트가 없습니다: " + truncate(response.body()));
			}
			// 역직렬화 = 스키마 검증. 실패 시 예외 -> 상위 재시도 루프
			return objectMapper.readValue(textNode.asText(), responseType);
		} catch (java.io.IOException e) {
			throw new IllegalStateException("Gemini 호출/파싱 실패: " + e.getMessage(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Gemini 호출이 중단되었습니다", e);
		}
	}

	private String endpoint() {
		return "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent";
	}

	private static String truncate(String s) {
		return s == null ? "" : (s.length() > 500 ? s.substring(0, 500) + "..." : s);
	}
}
