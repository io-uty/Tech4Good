package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. careWorkers.attendanceMonthly를 그대로 노출. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceMonthlyEntry {
	private String month;
	private int attendance;
	private int late;
	private int absence;
	private int vacation;
}
