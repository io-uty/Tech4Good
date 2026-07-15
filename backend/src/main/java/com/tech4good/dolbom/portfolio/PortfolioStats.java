package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. confirmed 방문일지 + careWorkers.attendanceMonthly로 계산하는 순수 통계. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStats {
	private int totalCheckins;
	private double totalHours;
	private int elderCount;
	/** attendanceMonthly 합산 기준 출근율(%) = attendance / (attendance+late+absence) * 100 */
	private double attendanceRate;
}
