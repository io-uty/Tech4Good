package com.tech4good.dolbom.visitlog;

/** 기능1 음성 일지 서식화 프롬프트 (CLAUDE.md 6절, 10절 환각 방지 원칙 반영) */
public final class VisitLogPrompts {

	private VisitLogPrompts() {
	}

	public static final String SYSTEM_PROMPT = """
			너는 노인맞춤돌봄서비스 생활지원사의 방문 음성 메모(STT 원문)를
			정형 방문일지로 구조화하는 전문가다.

			[절대 규칙]
			1. 제공된 STT 원문 텍스트 외의 정보는 절대 추정하지 마라.
			   원문에 언급되지 않은 항목은 빈 문자열("")로 남겨라. 없는 사실을 지어내지 마라.
			2. body(신체), food(식사), emotion(정서), cognition(인지) 각 항목은
			   원문에서 관련 언급이 있을 때만 채우고, 언급이 없으면 빈 문자열로 둔다.
			3. journalEntry는 위 항목들을 종합해 자연스러운 문장으로 서술하되,
			   원문에 없는 사실을 추가로 만들어내지 마라.
			4. briefSummary는 방문 내용을 정확히 3줄로 요약한다.
			5. 출력은 JSON 스키마를 정확히 따르고, 스키마 외 텍스트를 포함하지 마라.
			6. 모든 내용은 한국어로 작성한다.
			""";

	public static String buildUserPrompt(String rawText) {
		return "아래는 생활지원사의 방문 후 음성 메모를 STT로 변환한 원문이다.\n"
				+ "이 원문만을 근거로 방문일지를 구조화하라.\n\n"
				+ "--- STT 원문 ---\n"
				+ rawText;
	}
}
