package com.tech4good.dolbom.handover;

import java.util.List;

import com.tech4good.dolbom.domain.VisitLog;

/** 기능3 인수인계 카드 생성 프롬프트 (CLAUDE.md 8.4절 제약 반영) */
public final class HandoverPrompts {

	private HandoverPrompts() {
	}

	public static final String SYSTEM_PROMPT = """
			너는 노인맞춤돌봄서비스의 인수인계 카드 작성 전문가다.
			담당 생활지원사가 교체될 때, 신규 담당자가 첫 방문 전에 알아야 할 정보를
			방문일지 기록에서 추출해 한 장의 카드로 정리한다.

			[절대 규칙]
			1. 제공된 방문일지 텍스트 외의 정보는 절대 추정하지 마라.
			   근거 없는 항목은 빈 문자열("") 또는 빈 배열([])로 비워둬라.
			2. 정서 트리거(emotionalTriggers)는 최소 2개 이상의 "서로 다른" 방문일지에서
			   유사한 패턴이 반복 확인될 때만 기록하라. 1회성 언급은 트리거로 만들지 마라.
			   각 트리거에는 근거가 된 방문일지의 logId를 sourceLogIds에 반드시 전부 나열하라.
			   sourceLogIds에는 실제로 제공된 logId만 사용하라.
			3. 의료 정보(chronicConditions, medications)는 방문일지에 명시적으로 적힌
			   병명·약물명만 기재하라. 증상으로부터 병명을 추정하는 것을 절대 금지한다.
			4. 출력은 JSON 스키마를 정확히 따르고, 스키마 외 텍스트를 포함하지 마라.

			[작성 지침]
			- 신규 담당자가 바로 활용할 수 있게 구체적으로 쓴다 (예: "3월 배우자 기일 전후에는
			  안부 확인 빈도를 높이고 배우자 이야기를 먼저 꺼내지 않는 게 좋음").
			- avoidTopics 각 항목은 "화제 — 사유" 형태로 쓴다.
			- 모든 내용은 한국어로 작성한다.
			""";

	/** 시간순 정렬된 방문일지 배열을 유저 프롬프트로 직렬화 (CLAUDE.md 8.2절) */
	public static String buildUserPrompt(String elderName, List<VisitLog> logs) {
		StringBuilder sb = new StringBuilder();
		sb.append("어르신 성함: ").append(elderName).append("\n");
		sb.append("아래는 시간순으로 정렬된 확정 방문일지 ").append(logs.size()).append("건이다.\n");
		sb.append("이 기록만을 근거로 인수인계 카드를 작성하라.\n\n");
		for (VisitLog log : logs) {
			var s = log.getStructuredLog();
			sb.append("--- 방문일지 (logId: ").append(log.getLogId()).append(") ---\n");
			sb.append("방문일시: ").append(log.getVisitDateTime()).append("\n");
			if (s != null) {
				sb.append("서비스유형: ").append(nullSafe(s.getServiceType())).append("\n");
				sb.append("활동내용: ").append(nullSafe(s.getActivityDetail())).append("\n");
				sb.append("어르신상태: ").append(nullSafe(s.getElderCondition())).append("\n");
				sb.append("특이사항: ").append(nullSafe(s.getSpecialNote())).append("\n");
			}
			if (log.getRiskTags() != null && !log.getRiskTags().isEmpty()) {
				sb.append("위험태그: ").append(String.join(", ", log.getRiskTags())).append("\n");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	private static String nullSafe(String value) {
		return value == null ? "" : value;
	}
}
