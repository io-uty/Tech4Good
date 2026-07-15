# 케어EZ Backend (Spring Boot)

기능3 인수인계 카드 + 공통 ClaudeService 구현체. 전체 명세는 레포 루트 `CLAUDE.md` 참고.

## 실행 준비

1. JDK 21 필요 (Gradle은 wrapper 포함, 별도 설치 불필요)
2. `backend/.env.example`을 복사해 `backend/.env` 생성 후 값 채우기
   - `ANTHROPIC_API_KEY`: Claude API 키
   - `FIREBASE_CREDENTIALS_PATH`: Firebase 서비스 계정 키 JSON 절대경로

## 실행

```bash
cd backend
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

## 주요 API (기능3)

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/dev/seed` | 더미 데이터 시딩 (어르신 3명 + 확정 방문일지 20건, 멱등) |
| POST | `/api/handover-cards/{elderId}/generate` | 방문일지 기반 카드 초안 생성 |
| PUT | `/api/handover-cards/{cardId}/confirm` | 검토·수정 후 확정 |
| GET | `/api/handover-cards/{elderId}/latest` | 최신 카드 조회 |
| GET | `/api/handover-cards/{elderId}/history` | 버전 이력 조회 |
| GET | `/api/handover-cards/{cardId}/source-logs` | 카드 근거 원본 로그 (추적용) |

시더 elderId: `elder-001`(김순자), `elder-002`(박철수), `elder-003`(이복례)

## 공통 ClaudeService (기능1·2에서 재사용)

```java
@Autowired ClaudeService claudeService;

MyResponseType result = claudeService.generateStructured(
    systemPrompt, userPrompt, MyResponseType.class); // maxRetries 기본 2
```

- 응답 스키마는 `responseType` 클래스에서 자동 파생 (Anthropic Structured Outputs — 서버 측 스키마 강제)
- 필드 설명은 `@JsonPropertyDescription`으로 부여
- 재시도 후에도 실패 시 `ClaudeSchemaValidationException` — 임의값 채움 없음
- 요청/응답 원문은 `backend/logs/claude-io.log`에 기록

## 테스트

```bash
./gradlew test
```

`HandoverCardE2ETest`는 실제 Firestore/Claude API를 사용하는 완료 기준(CLAUDE.md 8.5절) 검증 테스트로,
`.env`에 키가 없으면 자동 skip된다.
