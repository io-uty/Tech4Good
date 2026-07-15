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
