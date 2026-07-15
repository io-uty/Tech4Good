package com.tech4good.dolbom.stt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 네이버 CLOVA Speech 장문 인식(local file upload) 연동.
 * - sync 모드: 응답 JSON의 text 필드에 전체 인식 결과가 담긴다.
 * - 제한: 파일 최대 2GB, 음성 최대 2시간(sync) — 방문 음성 메모에는 충분.
 */
@Slf4j
@Service
public class ClovaSpeechService {

	private final String invokeUrl;
	private final String secretKey;
	private final ObjectMapper objectMapper;
	private final RestClient restClient = RestClient.create();

	public ClovaSpeechService(
			@Value("${clova.speech.invoke-url}") String invokeUrl,
			@Value("${clova.speech.secret-key}") String secretKey,
			ObjectMapper firestoreObjectMapper) {
		this.invokeUrl = invokeUrl;
		this.secretKey = secretKey;
		this.objectMapper = firestoreObjectMapper;
	}

	/** 음성 파일 바이트 -> 인식된 전체 텍스트 */
	public String transcribe(byte[] audioBytes, String originalFilename) {
		if (invokeUrl == null || invokeUrl.isBlank() || secretKey == null || secretKey.isBlank()) {
			throw new IllegalStateException(
					"CLOVA Speech 설정이 없습니다. backend/.env의 CLOVA_SPEECH_INVOKE_URL / CLOVA_SPEECH_SECRET_KEY를 채워주세요.");
		}

		String filename = (originalFilename == null || originalFilename.isBlank()) ? "audio.wav" : originalFilename;

		ByteArrayResource media = new ByteArrayResource(audioBytes) {
			@Override
			public String getFilename() {
				return filename;
			}
		};

		HttpHeaders paramsHeaders = new HttpHeaders();
		paramsHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> params = new HttpEntity<>(
				"{\"language\":\"ko-KR\",\"completion\":\"sync\"}", paramsHeaders);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("media", media);
		body.add("params", params);

		String responseBody = restClient.post()
				.uri(invokeUrl + "/recognizer/upload")
				.header("X-CLOVASPEECH-API-KEY", secretKey)
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.retrieve()
				.body(String.class);

		try {
			JsonNode root = objectMapper.readTree(responseBody);
			JsonNode text = root.path("text");
			if (text.isMissingNode()) {
				throw new IllegalStateException("CLOVA Speech 응답에 text 필드가 없습니다: " + truncate(responseBody));
			}
			log.info("STT 완료: {} bytes -> {} chars", audioBytes.length, text.asText().length());
			return text.asText();
		} catch (com.fasterxml.jackson.core.JacksonException e) {
			throw new IllegalStateException("CLOVA Speech 응답 파싱 실패: " + truncate(responseBody), e);
		}
	}

	private static String truncate(String s) {
		return s == null ? "" : (s.length() > 500 ? s.substring(0, 500) + "..." : s);
	}
}
