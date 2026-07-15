package com.tech4good.dolbom.portfolio;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Claude structured output 항목 1건 - 긍정적 변화 사례. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntry {

	@JsonPropertyDescription("변화가 관찰된 어르신 ID")
	private String elderId;

	@JsonPropertyDescription("변화가 관찰된 기간 (예: 2025-03 ~ 2025-05)")
	private String period;

	@JsonPropertyDescription("관찰된 긍정적 변화 요약 (1문장)")
	private String change;

	@JsonPropertyDescription("일지에서 발췌·요약한 근거 (1~2문장)")
	private String evidence;
}
