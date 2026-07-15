package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. careWorkers.attendanceMonthly 합산 + visitLogs 상태 비율로 계산. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceStats {
	private int totalWorkDays;
	private int absence;
	private int late;
	/** confirmed / 전체 방문일지 * 100 */
	private double logCompletionRate;
}
