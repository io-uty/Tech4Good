package com.tech4good.dolbom.portfolio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Claude structured output 최상위 래퍼. outputConfig(Class)는 최상위가 object여야 해서 한 겹 감쌈. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineResponse {

	@JsonPropertyDescription("사건 후보 목록 중 선별한 경력 타임라인 마일스톤 3~6건")
	private List<TimelineEntry> entries;
}
