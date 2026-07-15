package com.tech4good.dolbom.visitlog;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tech4good.dolbom.common.ApiExceptions.BadRequestException;
import com.tech4good.dolbom.common.stt.SttService;
import com.tech4good.dolbom.domain.VisitLog;

import lombok.RequiredArgsConstructor;

/** 기능1 음성 일지 서식화 API (신규 명세: /api/stt, /api/visit-logs) */
@RestController
@RequiredArgsConstructor
public class VisitLogController {

	private final SttService sttService;
	private final VisitLogService visitLogService;

	public record SttResponse(String rawText) {
	}

	public record CreateVisitLogRequest(String workerId, String elderId, String rawText) {
	}

	public record VisitLogResponse(
			String logId, String elderId, String workerId, String createdAt,
			String body, String food, String emotion, String cognition, String journalEntry) {

		static VisitLogResponse from(VisitLog log) {
			return new VisitLogResponse(
					log.getLogId(), log.getElderId(), log.getWorkerId(), log.getCreatedAt(),
					log.getBody(), log.getFood(), log.getEmotion(), log.getCognition(), log.getJournalEntry());
		}
	}

	/** 오디오 파일 → STT 텍스트 반환 (Naver Clova만 호출, Firestore 저장 없음) */
	@PostMapping(value = "/api/stt", consumes = "multipart/form-data")
	public SttResponse stt(@RequestParam("audioFile") MultipartFile audioFile) {
		if (audioFile == null || audioFile.isEmpty()) {
			throw new BadRequestException("audioFile은 필수입니다.");
		}
		byte[] bytes;
		try {
			bytes = audioFile.getBytes();
		} catch (IOException e) {
			throw new BadRequestException("audioFile을 읽을 수 없습니다: " + e.getMessage());
		}
		String rawText = sttService.transcribe(bytes, audioFile.getContentType());
		return new SttResponse(rawText);
	}

	/** STT 텍스트 → Claude 구조화 → Firestore 저장 → 저장된 문서 반환 */
	@PostMapping("/api/visit-logs")
	public VisitLogResponse create(@RequestBody CreateVisitLogRequest request) {
		if (request == null) {
			throw new BadRequestException("요청 본문이 비어 있습니다.");
		}
		VisitLog log = visitLogService.createFromRawText(request.workerId(), request.elderId(), request.rawText());
		return VisitLogResponse.from(log);
	}
}
