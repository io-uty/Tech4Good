package com.tech4good.dolbom.portfolio;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. careWorkers.experiences를 그대로 노출. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceItem {
	private String period;
	private String title;

	// Lombok이 isActive()를 getter로 생성하면 Jackson 기본 규칙상 "is" 접두어가 벗겨져
	// JSON 키가 "active"로 나가버림 -> 명세("isActive")와 어긋남. @JsonProperty로 고정.
	@JsonProperty("isActive")
	private boolean isActive;
}
