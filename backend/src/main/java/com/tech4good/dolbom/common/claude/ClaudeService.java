package com.tech4good.dolbom.common.claude;

/**
 * Claude API 호출을 감싸는 공통 서비스 계층 (CLAUDE.md 5절).
 * 기능1·2·3은 전부 이 인터페이스를 통해서만 LLM을 호출한다.
 *
 * 참고: 명세의 별도 JsonSchema 파라미터는 responseType 클래스에서 자동 파생되는
 * 스키마(Anthropic Structured Outputs)로 대체 — 서버 측에서 스키마가 강제되므로
 * 별도 스키마 정의 없이 응답 타입 클래스만 넘기면 된다.
 */
public interface ClaudeService {

	/** maxRetries 기본값 2로 호출 */
	<T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType);

	/**
	 * @param systemPrompt 시스템 프롬프트 (JSON-only 강제 문구는 내부에서 공통 삽입)
	 * @param userPrompt   유저 프롬프트
	 * @param responseType 응답 JSON 스키마의 원천이 되는 클래스
	 * @param maxRetries   스키마 검증 실패 시 재시도 횟수
	 * @return 스키마 검증을 통과한 응답 객체. 절대 임의값으로 채워 반환하지 않는다.
	 * @throws ClaudeSchemaValidationException 재시도 후에도 유효한 응답을 얻지 못한 경우
	 */
	<T> T generateStructured(String systemPrompt, String userPrompt, Class<T> responseType, int maxRetries);
}
