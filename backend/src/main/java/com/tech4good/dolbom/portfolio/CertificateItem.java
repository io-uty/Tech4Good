package com.tech4good.dolbom.portfolio;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** LLM 미사용. careWorkers.certificates를 그대로 노출. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateItem {
	private String title;
	private String date;
}
