package com.tech4good.dolbom.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** visitLogs 컬렉션 — 기능1 산출물, 기능3의 입력 (CLAUDE.md 4절 스키마 그대로) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitLog {
	private String logId;
	private String elderId;
	private String workerId;
	/** ISO-8601 문자열 (예: 2026-03-05T10:30:00) */
	private String visitDateTime;
	/** STT 원문 */
	private String rawSttText;
	private StructuredLog structuredLog;
	/** 위험 신호 태그 (룰 기반) */
	private List<String> riskTags;
	/** "draft" | "confirmed" */
	private String status;
	private String confirmedBy;
	private String confirmedAt;

	// ===== 아래는 신규 API 명세(POST /api/visit-logs, rawText 기반) 산출 필드 =====
	// rawText 자체는 저장하지 않는다. structuredLog/rawSttText는 기능3(HandoverPrompts) 호환을 위해
	// 이 필드들로부터 자동 파생되어 함께 채워진다.
	private String body;
	private String food;
	private String emotion;
	private String cognition;
	/** Claude가 생성한 방문일지 서술문 */
	private String journalEntry;
	/** 3줄 요약 */
	private List<String> briefSummary;
	private String createdAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class StructuredLog {
		/** 안전지원 / 사회참여 / 일상생활지원 */
		private String serviceType;
		private String activityDetail;
		private String elderCondition;
		private String specialNote;
	}
}
