package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. workerId의 confirmed 방문일지만으로 계산하는 순수 통계. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStats {
	private int totalCheckins;
	private int elderCount;
	private double totalHours;
}
