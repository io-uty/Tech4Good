package com.tech4good.dolbom.portfolio;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Claude structured output 항목 1건 - 경력 타임라인 마일스톤(신규배정/위험대응/근속마일스톤 등). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEntry {

	@JsonPropertyDescription("사건 발생 날짜, 입력으로 주어진 후보의 date를 그대로 사용 (예: 2026-03-05)")
	private String date;

	@JsonPropertyDescription("카드 제목 (1문장, 예: '김순자 어르신 신규 배정')")
	private String title;

	@JsonPropertyDescription("카드 부제 (1문장, 근거가 된 방문일지 내용 요약)")
	private String subtitle;

	@JsonPropertyDescription("아이콘 종류. 반드시 다음 중 하나: 'badge'(근속 마일스톤), 'user'(신규 어르신 배정), "
			+ "'chat'(정서적 변화/상담), 'hospital'(위험 신호 대응), 'star'(우수 사례) 중 입력 후보의 type과 매칭되는 값")
	private String iconType;
}
