package com.tech4good.dolbom.portfolio;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** GET /api/portfolio/{workerId} 최종 응답 (신규 명세 - 근태/차트/자격증/경력 확장). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
	private String workerId;
	private PortfolioStats stats;
	private AttendanceStats attendanceStats;
	private List<AttendanceMonthlyEntry> attendanceMonthly;
	private List<ActivityTrendEntry> activityTrends;
	private List<CarePerformanceItem> carePerformances;
	private List<ExperienceItem> experiences;
	private List<CertificateItem> certificates;
	private List<TimelineEntry> timeline;
}
