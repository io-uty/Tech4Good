package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. 연도별 담당 어르신 수(elderId distinct). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareHistoryYear {
	private int year;
	private int elderCount;
}
