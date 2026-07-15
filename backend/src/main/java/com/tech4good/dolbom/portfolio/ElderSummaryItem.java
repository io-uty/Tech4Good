package com.tech4good.dolbom.portfolio;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * careByElder 배치 요약 호출 결과 항목 1건.
 * elderName/period는 일부러 안 넣는다 - 그건 elders/최초 방문일에서 백엔드가 계산하는 값이라
 * Claude가 지어내면 안 되기 때문.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElderSummaryItem {

	@JsonPropertyDescription("어르신 ID (입력에 주어진 값 그대로)")
	private String elderId;

	@JsonPropertyDescription("성향, 특이사항, 돌봄 과정에서의 주요 변화를 담은 2~3문장 요약")
	private String summary;
}
