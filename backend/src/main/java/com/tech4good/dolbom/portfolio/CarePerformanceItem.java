package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. confirmed 방문일지의 serviceType 카운트를 UI 카테고리로 매핑. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarePerformanceItem {
	private String id;
	private String label;
	private String value;
	/** 'emotion' | 'medicine' | 'food' | 'hospital' | 'emergency' */
	private String iconType;
}
