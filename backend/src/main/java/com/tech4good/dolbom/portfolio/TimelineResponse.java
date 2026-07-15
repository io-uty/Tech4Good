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

	@JsonPropertyDescription("어르신의 정서적/신체적 상태가 긍정적으로 변화한 사례 3~5건")
	private List<TimelineEntry> entries;
}
