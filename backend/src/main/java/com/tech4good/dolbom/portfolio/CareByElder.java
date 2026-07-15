package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 어르신 1명에 대한 케어 카드.
 * elderId/elderName/period는 LLM 미사용(elders 조회 + 최초 방문일 계산), summary만 LLM.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareByElder {
	private String elderId;
	private String elderName;
	private String period;
	private String summary;
}
