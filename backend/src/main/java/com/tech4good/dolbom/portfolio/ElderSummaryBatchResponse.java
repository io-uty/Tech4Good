package com.tech4good.dolbom.portfolio;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** careByElder의 summary 배치 생성 호출에 쓰는 Claude structured output 최상위 래퍼. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElderSummaryBatchResponse {

	@JsonPropertyDescription("입력으로 주어진 모든 어르신 ID 각각에 대한 요약. 하나도 빠짐없이 포함해야 함")
	private List<ElderSummaryItem> summaries;
}
