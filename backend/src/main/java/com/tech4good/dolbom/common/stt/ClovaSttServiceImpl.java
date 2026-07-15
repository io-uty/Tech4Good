package com.tech4good.dolbom.common.stt;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Naver Clova Speech Recognition (단문 인식, CSR) REST API 구현체.
 * 엔드포인트: https://naveropenapi.apigw.ntruss.com/recog/v1/stt?lang=Kor
 * 헤더: X-NCP-APIGW-API-KEY-ID / X-NCP-APIGW-API-KEY, Content-Type: application/octet-stream
 * 응답: {"text": "..."}
 * 참고: 단문 인식 상품은 약 1분 이내의 오디오만 지원한다.
 */
@Slf4j
@Service
public class ClovaSttServiceImpl implements SttService {

	private static final String ENDPOINT = "https://naveropenapi.apigw.ntruss.com/recog/v1/stt?lang=Kor";

	@Value("${naver.clova.client-id:}")
	private String clientId;

	@Value("${naver.clova.client-secret:}")
	private String clientSecret;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String transcribe(byte[] audioBytes, String contentType) {
		if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
			throw new IllegalStateException(
					"NAVER_CLOVA_CLIENT_ID/NAVER_CLOVA_CLIENT_SECRET이 설정되지 않았습니다. backend/.env 파일을 채워주세요.");
		}
		if (audioBytes == null || audioBytes.length == 0) {
			throw new IllegalStateException("오디오 파일이 비어 있습니다.");
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(ENDPOINT))
				.header("X-NCP-APIGW-API-KEY-ID", clientId)
				.header("X-NCP-APIGW-API-KEY", clientSecret)
				.header("Content-Type", "application/octet-stream")
				.POST(HttpRequest.BodyPublishers.ofByteArray(audioBytes))
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				throw new IllegalStateException(
						"Naver Clova STT 호출 실패 (status=" + response.statusCode() + "): " + response.body());
			}
			var node = objectMapper.readTree(response.body());
			var textNode = node.get("text");
			if (textNode == null) {
				throw new IllegalStateException("Naver Clova STT 응답에 text 필드가 없습니다: " + response.body());
			}
			return textNode.asText();
		} catch (IOException e) {
			throw new IllegalStateException("Naver Clova STT 호출 중 네트워크 오류: " + e.getMessage(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Naver Clova STT 호출이 중단되었습니다", e);
		}
	}
}
