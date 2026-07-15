package com.tech4good.dolbom.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 음성 일지(STT 원문) 구조화 결과 스키마.
 * Claude Structured Outputs의 응답 스키마 원천 클래스 —
 * 필드 설명(@JsonPropertyDescription)이 그대로 LLM에게 전달되는 스키마 설명이 된다.
 *
 * 환각 방지 원칙(CLAUDE.md 10절): 원문에 없는 정보는 추정하지 않는다.
 * 언급 안 된 항목은 빈 문자열로 남긴다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitLogSummary {

	@JsonPropertyDescription("신체 상태 관련 기록 (거동, 통증, 복용 약물 등). 방문일지 원문에 언급된 내용만 기재하고, 언급이 없으면 빈 문자열로 둔다.")
	private String body;

	@JsonPropertyDescription("식사·영양 관련 기록. 원문에 언급된 내용만 기재하고, 언급이 없으면 빈 문자열로 둔다.")
	private String food;

	@JsonPropertyDescription("정서 상태 관련 기록 (기분, 감정 변화, 정서적 사건 등). 원문에 언급된 내용만 기재하고, 언급이 없으면 빈 문자열로 둔다.")
	private String emotion;

	@JsonPropertyDescription("인지 상태 관련 기록 (기억력, 지남력, 대화 이해도 등). 원문에 언급된 내용만 기재하고, 언급이 없으면 빈 문자열로 둔다.")
	private String cognition;

	@JsonPropertyDescription("위 body/food/emotion/cognition 내용을 종합해 자연스러운 문장으로 서술한 방문일지 본문. 원문에 없는 사실을 추정해 덧붙이지 않는다.")
	private String journalEntry;

	@JsonPropertyDescription("방문 내용을 요약한 3줄 요약. 정확히 3개의 문자열 항목으로 구성한다.")
	private List<String> briefSummary;
}
