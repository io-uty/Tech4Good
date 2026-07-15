# CLAUDE.md — 돌봄ON (가칭)

생활지원사·전담사회복지사 업무 지원 플랫폼. 이 문서는 프로젝트 전체 맥락과 기능별 명세를 담는다. Claude Code 세션 시작 시 이 파일을 항상 먼저 읽을 것.

---

## 1. 프로젝트 배경

**대상 사용자**: 노인맞춤돌봄서비스 생활지원사(SW), 전담사회복지사(ADMIN)
**최종 수혜자**: 돌봄 사각지대의 독거노인

**해결하려는 pain point 3가지**:

| # | Pain Point | 대응 기능 |
|---|---|---|
| 1 | 행정 서류(방문일지·상담기록) 입력에 업무시간 과다 소모, 심층 상담·신규 발굴 시간 부족 | 기능1: 음성 일지 서식화 |
| 2 | 잦은 인력 이탈(1년 단위 계약직) → 경력 증명·성과 정리 부담, 낮은 업무 만족도 | 기능2: 경력 포트폴리오 |
| 3 | 담당자 교체 시 어르신의 정성적 맥락(성향·가족관계·정서 트리거) 유실 | 기능3: 인수인계 카드 |

기능1과 기능3은 같은 데이터(방문 기록)를 다른 목적으로 가공한다는 점에서 연결되어 있음. 기능2는 SW 개인의 근무 이력을 다루는 독립적인 축.

---

## 2. 기술 스택

| 영역 | 선택 |
|---|---|
| Frontend | React |
| Backend | Spring Boot |
| DB | Firebase Firestore |
| STT | Naver Clova Speech (CSR) |
| LLM | Claude API |

---

## 3. 팀 및 작업 분담

| 기능 | 우선순위 | 작업량 | 담당 |
|---|---|---|---|
| 공통 ClaudeService | 최우선(선행) | 작음 | 박수지 |
| 기능1 음성 일지 서식화 | MUST | 중간 | 박수지 |
| 기능2 경력 포트폴리오 | SHOULD | 중간(stats 쉬움/timeline 걸림) | 정현민 |
| 기능3 인수인계 카드 | SHOULD | 중간 | 신승민 |
| PHQ-9 심화조사지 | 스트레치 | 큼 | 시간 되는 사람 / 신승민(기능3 완료 후) |

**의존 관계**: 공통 ClaudeService가 기능1·2·3의 선행 조건. 기능3은 기능1의 방문일지 데이터를 입력으로 사용하므로, 기능1의 Firestore 스키마(`visitLogs`)가 먼저 확정되어야 함.

---

## 4. Firestore 데이터 모델 (공통)

전 기능이 공유하는 컬렉션. 기능별로 신규 컬렉션 추가 시 이 섹션에 반드시 업데이트.

```
elders (어르신)
  - elderId (doc id)
  - name, birthDate, address, guardianContact
  - createdAt, updatedAt

careWorkers (생활지원사)
  - workerId (doc id)
  - name, org, hireDate, contractEndDate
  - assignedElderIds: [elderId, ...]

visitLogs (방문일지) — 기능1 산출물, 기능3의 입력
  - logId (doc id)
  - elderId, workerId
  - visitDateTime
  - rawSttText          // STT 원문
  - structuredLog: {
      serviceType,       // 안전지원/사회참여/일상생활지원
      activityDetail,
      elderCondition,
      specialNote
    }
  - riskTags: [string]   // 위험 신호 태그 (룰 기반)
  - status: "draft" | "confirmed"
  - confirmedBy, confirmedAt
```

---

## 5. 공통 모듈: ClaudeService (선행작업, 담당 박수지)

**목적**: Claude API 호출을 감싸는 단일 서비스 계층. 기능1·2·3 전부 이 서비스를 통해서만 LLM 호출.

**요구사항**:
- 입력: `systemPrompt`, `userPrompt`, `responseSchema`(JSON schema), `maxRetries`(기본 2)
- 출력: 스키마 검증 통과한 JSON 객체. 검증 실패 시 오류 메시지를 포함해 재호출(최대 `maxRetries`), 최종 실패 시 명시적 예외(`ClaudeSchemaValidationException`) — **절대 임의값으로 채워서 반환하지 않음**
- 프롬프트에 "JSON만 출력, 설명 텍스트 금지" 강제 문구 공통 삽입
- 로깅: 요청/응답 원문을 별도 로그(Firestore 또는 파일)에 남겨 디버깅 가능하게 함
- 타임아웃·API 에러는 상위로 명확한 예외 전파 (개별 기능에서 재정의하지 않음)

**인터페이스 예시 (Java)**:
```java
public interface ClaudeService {
    <T> T generateStructured(
        String systemPrompt,
        String userPrompt,
        Class<T> responseType,
        JsonSchema schema
    );
}
```

기능1·2·3은 이 인터페이스만 호출하고, 프롬프트 내용만 각자 관리한다.

---

## 6. 기능1: 음성 일지 서식화 (담당: 박수지)

**목적**: SW의 방문 후 음성 메모를 정형 일지로 자동 변환, 검토·확정 후 저장.

**파이프라인**: 음성 녹음 → Naver Clova STT → ClaudeService(구조화 추출) → 초안 편집 UI → 확정 → `visitLogs` 저장

**ClaudeService 호출 스펙**:
- input: STT 원문 텍스트
- output schema: `structuredLog` (섹션 4 참조) + `riskTags` 후보(LLM 제안, 최종 확정은 룰 엔진과 병행)
- 시스템 프롬프트 원칙: 원문에 없는 정보 추정 금지, 언급 안 된 필드는 `null` 유지

**API**:
| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/visit-logs/transcribe` | 음성파일 → STT 텍스트 반환 |
| POST | `/api/visit-logs/draft` | STT 텍스트 → 구조화 초안 생성 |
| PUT | `/api/visit-logs/{logId}/confirm` | 수정된 초안 확정 저장 |
| GET | `/api/visit-logs/{elderId}` | 어르신별 일지 목록 조회 |

**완료 기준**: STT 텍스트 입력 시 3초 내 구조화 초안 생성, 미언급 필드는 `null`로 남아 있어야 함(자동 채움 금지 검증 테스트 포함).

---

## 7. 기능2: 경력 포트폴리오 (담당: 정현민)

> **가정 명시**: 이 기능은 어르신이 아니라 **SW 본인의 근무 이력**을 자동으로 정리해주는 기능으로 이해하고 작성함. 이직률이 높은 직군 특성상, 퇴사·이직 시 활용 가능한 경력 증명 자료를 자동 생성해 업무 만족도를 높이는 목적. 실제 의도와 다르면 수정 필요.

**목적**: `visitLogs`, 담당 어르신 수, 근무 기간 등을 집계해 SW 개인의 경력 카드를 자동 생성.

**구성 요소**:

| 요소 | 내용 | 난이도 |
|---|---|---|
| Stats | 총 방문 횟수, 담당 어르신 수, 근속 기간, 서비스 유형별 비율 | 쉬움 — Firestore 집계 쿼리로 해결, LLM 불필요 |
| Timeline | 근무 기간 중 주요 사건(신규 어르신 배정, 위험 발견·조치 이력, 장기근속 마일스톤)을 시간순 카드로 시각화 | 어려움 — 원본 로그에서 "주요 사건" 선별에 LLM 판단 필요 |

**데이터 모델 추가**:
```
careerRecords (careerId = workerId)
  - workerId
  - stats: { totalVisits, totalElders, tenureMonths, serviceTypeBreakdown }
  - timelineEvents: [
      { date, eventType, description, sourceLogId }
    ]
  - generatedAt
```

**ClaudeService 호출**: timeline 이벤트 선별 시에만 사용. stats는 순수 쿼리로 처리하고 LLM 호출 안 함(비용·지연 최소화).

**API**:
| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/career/{workerId}/stats` | 통계 조회 |
| POST | `/api/career/{workerId}/timeline/generate` | 방문일지 기반 타임라인 생성 |
| GET | `/api/career/{workerId}/timeline` | 타임라인 조회 |

**완료 기준**: stats는 즉시 조회 가능, timeline은 최소 3개 이벤트 유형(신규배정/위험대응/근속마일스톤) 자동 분류.

---

## 8. 기능3: 인수인계 카드 (담당: 신승민 — 이번 세션 대상)

**목적**: 담당 SW 교체 시, 신규 담당자가 첫 방문 전에 알아야 할 정보를 한 장의 카드로 자동 생성. 정성적 맥락(성향, 가족관계, 정서 트리거, 금기 화제)의 유실을 방지.

### 8.1 데이터 모델

```
handoverCards (cardId = elderId 기준 최신본 1개 + 이력)
  - elderId
  - generatedAt
  - previousWorkerId, newWorkerId (nullable — 최초 생성 시 null)
  - sourceLogRange: { fromDate, toDate, logCount }   // 어떤 기간의 일지를 참조했는지
  - summary: {
      basicInfo: { livingCondition, familyRelation, chronicConditions, medications },
      personality: string,          // 성향, 대화 성향
      emotionalTriggers: [
        { trigger, description, sourceLogId }   // ex. "매년 3월 배우자 기일 전후 우울감 심화"
      ],
      preferredTopics: [string],    // 좋아하는 화제
      avoidTopics: [string],        // 금기 화제 + 사유
      recentThreeMonthSummary: string
    }
  - version: int
  - previousVersionId: string (nullable)
```

**버전 관리 원칙**: 카드는 매번 새 버전으로 저장하고 이전 버전은 보존(F-3.4 요구사항). `elders/{elderId}`에는 최신 `cardId`만 참조 필드로 저장.

### 8.2 파이프라인

```
[트리거]
  ADMIN이 "인수인계 카드 생성" 버튼 클릭 (담당자 교체 시 수동 트리거)
        ↓
[데이터 수집]
  해당 elderId의 visitLogs 전체(또는 최근 N개월) 조회
  status="confirmed"인 로그만 사용 — 미확정 초안은 제외
        ↓
[ClaudeService 호출]
  입력: 시간순 정렬된 structuredLog 배열 + specialNote 전체
  출력 스키마: summary 객체 (8.1 참조)
  프롬프트 원칙:
    - 반드시 원본 로그의 sourceLogId를 근거로 명시 (환각 방지, 추적 가능성)
    - 정서 트리거는 "반복 패턴이 로그 2건 이상에서 확인될 때만" 생성 — 1회성 언급은 제외
    - 의료 정보(chronicConditions, medications)는 로그에 명시된 것만 기재, 추정 금지
        ↓
[검토 화면]
  ADMIN 또는 기존 SW가 생성된 카드 검토 → 수정 가능 → 확정
        ↓
[저장 및 배포]
  handoverCards에 새 버전 저장
  신규 SW 앱에 "첫 방문 가이드" 카드로 표시
```

### 8.3 API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/handover-cards/{elderId}/generate` | 방문일지 기반 카드 초안 생성 |
| PUT | `/api/handover-cards/{cardId}/confirm` | 검토·수정 후 확정 |
| GET | `/api/handover-cards/{elderId}/latest` | 최신 카드 조회 |
| GET | `/api/handover-cards/{elderId}/history` | 버전 이력 조회 |
| GET | `/api/handover-cards/{cardId}/source-logs` | 카드 근거가 된 원본 로그 목록 (추적용) |

### 8.4 프롬프트 설계 방향

시스템 프롬프트에 반드시 포함할 제약:
1. "제공된 방문일지 텍스트 외의 정보는 절대 추정하지 마라. 근거 없는 항목은 필드를 비워둬라."
2. "정서 트리거는 최소 2개 이상의 서로 다른 방문일지에서 유사 패턴이 반복될 때만 기록하라. 근거가 된 로그의 ID를 반드시 포함하라."
3. "출력은 JSON 스키마를 정확히 따르고, 스키마 외 텍스트를 포함하지 마라."

### 8.5 완료 기준 (MVP)

- 방문일지 5건 이상의 더미 데이터로 카드 생성 시, `emotionalTriggers` 각 항목에 `sourceLogId`가 최소 2개 이상 연결되어야 함
- 로그에 없는 정보(예: 특정 병명)를 임의로 채우지 않는지 검증 테스트 포함
- 카드 재생성 시 이전 버전이 삭제되지 않고 `previousVersionId`로 연결되는지 확인

### 8.6 프론트엔드 흐름 (React)

- ADMIN 대시보드: 어르신 목록 → "담당자 변경" 표시된 케이스에 "인수인계 카드 생성" 버튼
- 카드 상세 화면: `basicInfo` / `personality` / `emotionalTriggers`(근거 로그 링크 포함) / `preferredTopics` / `avoidTopics` 섹션별 표시
- 신규 SW 앱: 첫 로그인 시 배정된 어르신의 최신 카드를 온보딩 화면으로 노출

---

## 9. PHQ-9 심화조사지 (스트레치, 신규 영역)

기능1의 음성 일지와 별개로, 정신건강 선별조사(우울/자살생각/고독감) 서식을 STT→JSON 구조화하는 작업. 별도 세션에서 JSON 스키마 초안 작성 완료 (`assessment_type`, `depression_phq9`, `suicide_ideation_sbq_r`, `loneliness_ucla3` 3개 영역, 문항별 `score_map` 방식).

**핵심 원칙(재확인)**: 점수 산출은 LLM이 아닌 룰(score_map lookup)로 계산. 언급 안 된 문항은 자동으로 0점 채우지 말고 `null` + `unmapped_or_ambiguous`에 사유 기록. 이 영역은 임상 척도이므로 기능1·3보다 훨씬 엄격한 검증 필요. 착수 전 스키마 파일 위치 확인 후 이어서 작업할 것.

---

## 10. 공통 규칙

- **환각 방지 원칙(전 기능 공통)**: LLM 출력에서 원본 텍스트에 없는 사실 추정 금지. 애매하면 필드를 비우고 사람이 확인하게 한다.
- **Firestore 보안 규칙**: SW는 자신에게 배정된 elderId 문서만 read/write, ADMIN은 소속 기관 전체 read, write는 confirm 액션에 한정.
- **민감정보**: `elders`, `handoverCards`, PHQ-9 관련 컬렉션은 건강·정서 정보 포함 — 접근 로그 별도 기록, 음성 원본은 STT 완료 후 삭제(설정 가능).
- **커밋 컨벤션**: `feat(기능1): ...`, `feat(기능3): ...` 형식으로 기능 태그 명시.
- **환경변수**: Claude API Key, Naver Clova API Key는 `.env`로 관리, 레포에 커밋 금지.