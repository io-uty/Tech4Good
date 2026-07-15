package com.tech4good.dolbom.visitlog;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.tech4good.dolbom.common.ApiExceptions.BadRequestException;
import com.tech4good.dolbom.common.claude.ClaudeService;
import com.tech4good.dolbom.domain.VisitLog;
import com.tech4good.dolbom.domain.VisitLogSummary;
import com.tech4good.dolbom.repository.VisitLogRepository;

import lombok.RequiredArgsConstructor;

/** 기능1 음성 일지 서식화 — rawText 기반 방문일지 생성 (신규 API 명세) */
@Service
@RequiredArgsConstructor
public class VisitLogService {

	private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter DATE_PREFIX = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final DateTimeFormatter ISO_WITH_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

	private final ClaudeService claudeService;
	private final VisitLogRepository visitLogRepository;

	public VisitLog createFromRawText(String workerId, String elderId, String rawText) {
		if (workerId == null || workerId.isBlank()) {
			throw new BadRequestException("workerId는 필수입니다.");
		}
		if (elderId == null || elderId.isBlank()) {
			throw new BadRequestException("elderId는 필수입니다.");
		}
		if (rawText == null || rawText.isBlank()) {
			throw new BadRequestException("rawText는 필수입니다.");
		}

		VisitLogSummary summary = claudeService.generateStructured(
				VisitLogPrompts.SYSTEM_PROMPT, VisitLogPrompts.buildUserPrompt(rawText), VisitLogSummary.class);

		ZonedDateTime now = ZonedDateTime.now(SEOUL);
		String createdAt = now.format(ISO_WITH_OFFSET);
		String logId = generateLogId(now);

		VisitLog log = VisitLog.builder()
				.logId(logId)
				.elderId(elderId)
				.workerId(workerId)
				.visitDateTime(createdAt)
				.createdAt(createdAt)
				// STT 원문은 검수 및 재처리를 위해 함께 저장
				.rawSttText(rawText)
				.body(nullToEmpty(summary.getBody()))
				.food(nullToEmpty(summary.getFood()))
				.emotion(nullToEmpty(summary.getEmotion()))
				.cognition(nullToEmpty(summary.getCognition()))
				.journalEntry(nullToEmpty(summary.getJournalEntry()))
				.briefSummary(summary.getBriefSummary())
				// 기능3(HandoverPrompts) 호환을 위해 구버전 필드도 함께 파생·저장
				.structuredLog(VisitLog.StructuredLog.builder()
						.activityDetail(nullToEmpty(summary.getBody()))
						.elderCondition(joinNonBlank(summary.getFood(), summary.getCognition()))
						.specialNote(nullToEmpty(summary.getEmotion()))
						.build())
				.riskTags(java.util.List.of())
				.status("confirmed")
				.confirmedBy(workerId)
				.confirmedAt(createdAt)
				.build();

		visitLogRepository.save(log);
		return log;
	}

	/** "log_YYYYMMDD_NNN" 형식 — 해당 날짜 접두사를 가진 기존 문서 수 + 1로 채번 */
	private String generateLogId(ZonedDateTime now) {
		String prefix = "log_" + now.format(DATE_PREFIX) + "_";
		int seq = visitLogRepository.countByLogIdPrefix(prefix) + 1;
		return prefix + String.format("%03d", seq);
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static String joinNonBlank(String a, String b) {
		StringBuilder sb = new StringBuilder();
		if (a != null && !a.isBlank()) {
			sb.append(a);
		}
		if (b != null && !b.isBlank()) {
			if (sb.length() > 0) {
				sb.append(" / ");
			}
			sb.append(b);
		}
		return sb.toString();
	}
}
