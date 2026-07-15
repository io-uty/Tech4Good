package com.tech4good.dolbom.portfolio;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 기능2(포트폴리오) 프롬프트 모음.
 * timeline은 백엔드가 신규배정/위험대응/근속마일스톤 후보를 결정론적으로 뽑아 넘기고,
 * Claude는 그중 가장 주요한 것을 골라 date/title/subtitle/iconType으로 다듬기만 한다
 * (환각 방지 원칙 — 후보에 없는 사건을 지어내지 않는다).
 */
public final class PortfolioPrompts {

	public static final String TIMELINE_SYSTEM_PROMPT = """
			당신은 노인 돌봄 생활지원사의 경력 타임라인을 정리하는 전문가입니다.
			아래 [사건 후보 목록]은 방문일지·근속 정보에서 규칙 기반으로 이미 추출된 사실입니다.
			이 목록에 없는 사건을 새로 만들어내지 마세요.
			후보 중 이력서에 넣을 만큼 의미 있는 사건 3~6건을 골라, 각각 title(1문장)과
			subtitle(근거 요약 1문장)을 자연스럽게 다듬어 반환하세요.
			date와 iconType은 후보에 주어진 값을 그대로 사용하세요.
			""";

	private PortfolioPrompts() {
	}

	public record MilestoneCandidate(String date, String type, String iconType, String rawDescription) {
	}

	public static String buildTimelineUserPrompt(List<MilestoneCandidate> candidates) {
		String candidateText = candidates.stream()
				.map(c -> String.format("- date: %s / type: %s / iconType: %s / 내용: %s",
						c.date(), c.type(), c.iconType(), c.rawDescription()))
				.collect(Collectors.joining("\n"));
		return "[사건 후보 목록]\n" + candidateText;
	}
}
