package com.tech4good.dolbom.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * careWorkers 컬렉션 (CLAUDE.md 4절).
 * certificates/experiences/attendanceMonthly는 실제 근태·인사 시스템이 없어 더미 시드로 채운다
 * (DevSeedService 참고). 실 서비스 연동 시 이 필드들의 출처를 근태 시스템으로 교체해야 한다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareWorker {
	private String workerId;
	private String name;
	private String org;
	/** ISO-8601 날짜 (예: 2024-09-01) */
	private String hireDate;
	/** nullable — 계약 종료일 없으면 현재 재직 중 */
	private String contractEndDate;
	private List<String> assignedElderIds;
	private List<Certificate> certificates;
	private List<Experience> experiences;
	/** 월별 근태 집계 (시간순) */
	private List<MonthlyAttendance> attendanceMonthly;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Certificate {
		private String title;
		/** 예: "2023.06" */
		private String date;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Experience {
		/** 예: "2024.09 ~ 현재" */
		private String period;
		private String title;
		private boolean isActive;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MonthlyAttendance {
		/** 예: "1월" */
		private String month;
		private int attendance;
		private int late;
		private int absence;
		private int vacation;
	}
}
