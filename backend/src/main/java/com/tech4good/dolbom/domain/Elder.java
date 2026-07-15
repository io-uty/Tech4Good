package com.tech4good.dolbom.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** elders 컬렉션 (CLAUDE.md 4절) */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Elder {
	private String elderId;
	private String name;
	private String birthDate;
	private String address;
	private String guardianContact;
	/** 최신 인수인계 카드 참조 (CLAUDE.md 8.1절 버전 관리 원칙) */
	private String latestCardId;
	private String createdAt;
	private String updatedAt;
}
