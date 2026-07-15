package com.tech4good.dolbom.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** handoverCards 컬렉션 (CLAUDE.md 8.1절). 매 생성마다 새 버전으로 저장, 이전 버전 보존. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverCard {
	private String cardId;
	private String elderId;
	private String generatedAt;
	private String previousWorkerId;
	private String newWorkerId;
	private SourceLogRange sourceLogRange;
	/** 카드 생성에 입력으로 사용된 방문일지 logId 전체 (source-logs 추적용) */
	private List<String> sourceLogIds;
	private HandoverSummary summary;
	private int version;
	private String previousVersionId;
	/** "draft" | "confirmed" */
	private String status;
	private String confirmedBy;
	private String confirmedAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SourceLogRange {
		private String fromDate;
		private String toDate;
		private int logCount;
	}
}
