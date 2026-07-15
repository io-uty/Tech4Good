package com.tech4good.dolbom.portfolio;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.tech4good.dolbom.domain.VisitLog;

/** 기능2(포트폴리오) 프롬프트 모음. HandoverPrompts와 같은 패턴을 따른다. */
public final class PortfolioPrompts {

	public static final String TIMELINE_SYSTEM_PROMPT = """
			당신은 노인 돌봄 생활지원사의 누적 방문 일지를 분석하는 전문가입니다.
			아래 방문 일지 목록에서 어르신의 정서적/신체적 상태가 긍정적으로 변화한 사례를
			근거와 함께 3~5건 골라주세요.
			""";

	public static final String CARE_BY_ELDER_SYSTEM_PROMPT = """
			당신은 노인 돌봄 생활지원사의 방문 일지를 분석하는 전문가입니다.
			여러 어르신별로 누적된 방문 일지 목록을 받아,
			각 어르신마다 성향, 특이사항, 돌봄 과정에서의 주요 변화를 2~3문장으로 요약합니다.
			입력에 있는 모든 어르신 ID에 대해 빠짐없이 하나씩 응답하세요.
			""";

	private PortfolioPrompts() {
	}

	public static String buildTimelineUserPrompt(List<VisitLog> logs) {
		String logsText = logs.stream()
				.map(PortfolioPrompts::formatLogLine)
				.collect(Collectors.joining("\n"));
		return "[방문 일지 목록]\n" + logsText;
	}

	public static String buildCareByElderUserPrompt(Set<String> elderIds, Map<String, List<VisitLog>> logsByElder) {
		StringBuilder sb = new StringBuilder();
		for (String elderId : elderIds) {
			sb.append("### 어르신 ID: ").append(elderId).append("\n");
			for (VisitLog log : logsByElder.get(elderId)) {
				sb.append("- ").append(formatLogText(log)).append("\n");
			}
			sb.append("\n");
		}
		return "[어르신별 일지 목록]\n" + sb;
	}

	private static String formatLogLine(VisitLog log) {
		return String.format("[%s] 어르신ID: %s / 내용: %s",
				log.getVisitDateTime(), log.getElderId(), formatLogText(log));
	}

	private static String formatLogText(VisitLog log) {
		VisitLog.StructuredLog s = log.getStructuredLog();
		if (s == null) {
			// structuredLog가 아직 없으면(후처리 전 등) STT 원문으로 대체
			return String.valueOf(log.getRawSttText());
		}
		return String.format("서비스유형: %s / 활동내용: %s / 어르신상태: %s / 특이사항: %s",
				s.getServiceType(), s.getActivityDetail(), s.getElderCondition(), s.getSpecialNote());
	}
}
