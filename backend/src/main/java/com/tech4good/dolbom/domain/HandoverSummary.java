package com.tech4good.dolbom.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 인수인계 카드의 summary 객체 (CLAUDE.md 8.1절).
 * Claude Structured Outputs의 응답 스키마 원천 클래스이기도 하다 —
 * 필드 설명(@JsonPropertyDescription)이 그대로 LLM에게 전달되는 스키마 설명이 된다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandoverSummary {

	@JsonPropertyDescription("기본 정보. 방문일지에 명시된 내용만 기재하고, 근거가 없으면 빈 문자열/빈 배열로 둔다.")
	private BasicInfo basicInfo;

	@JsonPropertyDescription("어르신의 성향과 대화 성향. 방문일지에서 관찰된 내용만 근거로 서술.")
	private String personality;

	@JsonPropertyDescription("정서 트리거 목록. 서로 다른 방문일지 2건 이상에서 반복 확인된 패턴만 기재.")
	private List<EmotionalTrigger> emotionalTriggers;

	@JsonPropertyDescription("어르신이 좋아하는 화제 목록")
	private List<String> preferredTopics;

	@JsonPropertyDescription("피해야 할 금기 화제 목록. 각 항목에 사유를 함께 서술.")
	private List<String> avoidTopics;

	@JsonPropertyDescription("최근 3개월 방문 기록 요약")
	private String recentThreeMonthSummary;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BasicInfo {
		@JsonPropertyDescription("거주 형태 및 생활 환경. 근거 없으면 빈 문자열.")
		private String livingCondition;

		@JsonPropertyDescription("가족 관계. 방문일지에 언급된 내용만. 근거 없으면 빈 문자열.")
		private String familyRelation;

		@JsonPropertyDescription("지병 목록. 방문일지에 명시된 병명만 기재, 추정 절대 금지. 근거 없으면 빈 배열.")
		private List<String> chronicConditions;

		@JsonPropertyDescription("복용 약물 목록. 방문일지에 명시된 약물만 기재, 추정 절대 금지. 근거 없으면 빈 배열.")
		private List<String> medications;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class EmotionalTrigger {
		@JsonPropertyDescription("트리거 요약 (예: 매년 3월 배우자 기일 전후 우울감 심화)")
		private String trigger;

		@JsonPropertyDescription("트리거에 대한 구체적 설명과 신규 담당자를 위한 대응 팁")
		private String description;

		@JsonPropertyDescription("근거가 된 방문일지 logId 목록. 반드시 서로 다른 로그 2개 이상.")
		private List<String> sourceLogIds;
	}
}
