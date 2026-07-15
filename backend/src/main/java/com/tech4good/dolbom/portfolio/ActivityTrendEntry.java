package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. confirmed 방문일지를 월별로 집계. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityTrendEntry {
	private String month;
	private int visits;
	private double hours;
	private int elders;
}
