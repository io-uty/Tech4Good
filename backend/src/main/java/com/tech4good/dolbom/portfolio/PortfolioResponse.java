package com.tech4good.dolbom.portfolio;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** GET /api/portfolio/{workerId} 최종 응답. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
	private String workerId;
	private PortfolioStats stats;
	private List<CareHistoryYear> careHistoryByYear;
	private List<CareByElder> careByElder;
	/** TODO: 이전 명세서 필드 구조 미전달 - 우선 빈 리스트로 내려감 */
	private List<Map<String, Object>> improvementByYear;
	private List<TimelineEntry> timeline;
}
