# 돌봄EZ (돌봄ON)

> 노인맞춤돌봄서비스 **생활지원사(SW)·전담사회복지사(ADMIN)** 업무 지원 플랫폼
> Tech4Good 해커톤 프로젝트

행정 서류 작성에 쓰이는 시간을 줄이고, 그 시간을 어르신에 대한 깊이 있는 돌봄으로 되돌려주는 것을 목표로 합니다.

---

## 목차

- [왜 만들었나](#왜-만들었나)
- [핵심 기능](#핵심-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [프로젝트 구조](#프로젝트-구조)
- [시작하기](#시작하기)
- [API 레퍼런스](#api-레퍼런스)
- [Firestore 데이터 모델](#firestore-데이터-모델)
- [설계 원칙](#설계-원칙)
- [팀](#팀)

---

## 왜 만들었나

노인맞춤돌봄서비스 현장에는 세 가지 고질적인 문제가 있습니다.

| # | 문제 | 대응 기능 |
|---|---|---|
| 1 | 방문일지·상담기록 등 행정 서류 입력에 업무시간이 과다하게 소모되어, 정작 심층 상담이나 신규 발굴에 쓸 시간이 부족하다 | **기능1. 음성 일지 서식화** |
| 2 | 1년 단위 계약직이라는 고용 특성상 잦은 인력 이탈이 발생하고, 그때마다 경력 증명·성과 정리 부담이 커 업무 만족도가 낮다 | **기능2. 경력 포트폴리오** |
| 3 | 담당자가 교체될 때마다 어르신의 정성적 맥락(성향·가족관계·정서 트리거·금기 화제)이 유실되어, 신규 담당자가 처음부터 다시 관계를 쌓아야 한다 | **기능3. 인수인계 카드** |

기능1과 기능3은 같은 원천 데이터(방문 기록)를 서로 다른 목적으로 가공한다는 점에서 연결되어 있고, 기능2는 SW 개인의 근무 이력을 다루는 독립적인 축입니다.

## 핵심 기능

### 기능1. 음성 일지 서식화

방문 후 SW가 음성으로 남긴 메모를 정형화된 일지로 자동 변환합니다.

```
음성 녹음 → Naver Clova STT → ClaudeService(구조화 추출) → 초안 편집 UI → 확정 → visitLogs 저장
```

- STT 원문에 없는 정보는 절대 추정하지 않고, 언급되지 않은 필드는 `null`로 남겨둡니다.
- 위험 신호 태그(`riskTags`)는 LLM 제안 + 룰 엔진 병행으로 검증합니다.

### 기능2. 경력 포트폴리오

`visitLogs`, 담당 어르신 수, 근무 기간 등을 집계해 SW 개인의 경력 카드를 자동 생성합니다.

| 구성 요소 | 내용 | 난이도 |
|---|---|---|
| Stats | 총 방문 횟수, 담당 어르신 수, 근속 기간, 서비스 유형별 비율 | 쉬움 — Firestore 집계 쿼리로 처리, LLM 미사용 |
| Timeline | 신규 배정·위험 대응·장기근속 마일스톤 등 주요 사건을 시간순으로 시각화 | 어려움 — 원본 로그에서 "주요 사건"을 선별하는 데 LLM 판단 필요 |

### 기능3. 인수인계 카드

담당 SW가 교체될 때, 신규 담당자가 첫 방문 전에 알아야 할 정보를 한 장의 카드로 자동 생성합니다.

```
ADMIN "인수인계 카드 생성" 클릭
      ↓
확정된(status=confirmed) 방문일지 수집
      ↓
ClaudeService 호출 → summary 생성 (근거 sourceLogId 포함)
      ↓
검토 화면에서 ADMIN/SW가 수정 → 확정
      ↓
handoverCards에 새 버전 저장 (이전 버전 보존) → 신규 SW 앱에 온보딩 카드로 노출
```

- 정서 트리거는 **서로 다른 로그 2건 이상**에서 반복 확인된 패턴만 기록하고, 근거가 된 `sourceLogId`를 함께 남깁니다.
- 의료 정보(지병, 복용 약물)는 로그에 명시된 것만 기재하며 추정하지 않습니다.

## 기술 스택

| 영역 | 선택 | 비고 |
|---|---|---|
| Frontend | React 19 + TypeScript + Vite + Tailwind CSS v4 | `lucide-react`, `recharts` 사용 |
| Backend | Spring Boot 4.1.0 (Java 21) | Gradle, Lombok |
| DB | Firebase Firestore | `firebase-admin` SDK |
| STT | Naver Clova Speech (CSR) | 음성 → 텍스트 변환 |
| LLM | Claude API (Anthropic Structured Outputs) | `anthropic-java` SDK |

## 아키텍처

기능1·2·3은 모두 하나의 공통 서비스 계층인 **`ClaudeService`** 를 통해서만 LLM을 호출합니다. "모델은 하나, 프롬프트는 셋"이라는 원칙 아래, 각 기능은 프롬프트와 응답 스키마만 개별적으로 관리합니다.

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

- 응답 스키마는 `responseType` 클래스의 `@JsonPropertyDescription` 어노테이션에서 자동 파생됩니다 (Anthropic Structured Outputs — 서버 측 스키마 강제).
- 스키마 검증에 최종 실패하면 `ClaudeSchemaValidationException`을 던질 뿐, **임의값으로 채워서 반환하지 않습니다.**
- 모든 요청/응답 원문은 `backend/logs/claude-io.log`에 기록되어 디버깅에 활용됩니다.

## 프로젝트 구조

```
Tech4Good/
├── CLAUDE.md              # 프로젝트 전체 명세 (기능별 상세 스펙 포함)
├── frontend/                # React + Vite
│   └── src/
│       ├── app/              # 앱 진입점, 전역 스타일
│       ├── pages/             # 화면 단위 (home, voice-log, handover, portfolio, checklist)
│       ├── entities/           # 도메인 목데이터
│       ├── shared/              # api 클라이언트, 공용 타입, 공용 UI
│       └── widgets/              # 하단 내비게이션 등 재사용 위젯
└── backend/                  # Spring Boot
    └── src/main/java/com/tech4good/dolbom/
        ├── common/             # 공통 유틸
        ├── config/              # Firebase/앱 설정
        ├── domain/               # Elder, VisitLog, HandoverCard, CareWorker 등
        ├── repository/            # FirestoreSupport 기반 리포지토리
        ├── visitlog/               # 기능1 컨트롤러·서비스·프롬프트
        ├── handover/                # 기능3 컨트롤러
        ├── portfolio/                # 기능2 컨트롤러·서비스
        └── dev/                       # 더미 데이터 시더
```

## 시작하기

### 1. 사전 준비

- Node.js (frontend)
- JDK 21 (backend — Gradle Wrapper 포함, 별도 설치 불필요)
- Anthropic API Key, Naver Clova API Key, Firebase 서비스 계정 키

### 2. Backend 실행

```bash
cd backend
cp .env.example .env   # 값 채우기: ANTHROPIC_API_KEY, FIREBASE_CREDENTIALS_PATH, NAVER_CLOVA_CLIENT_ID/SECRET
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

기본 포트는 `8080`입니다. 더미 데이터가 필요하면 서버 기동 후 아래를 호출하세요 (어르신 3명 + 확정 방문일지 20건, 멱등):

```bash
curl -X POST http://localhost:8080/api/dev/seed
```

### 3. Frontend 실행

```bash
cd frontend
npm install
npm run dev
```

| 스크립트 | 설명 |
|---|---|
| `npm run dev` | 개발 서버 (Vite) |
| `npm run build` | 타입체크(`tsc`) 후 프로덕션 빌드 |
| `npm run preview` | 빌드 결과 미리보기 |

### 4. 테스트

```bash
cd backend
./gradlew test
```

`HandoverCardE2ETest`는 실제 Firestore/Claude API를 사용하는 완료 기준 검증 테스트로, `.env`에 키가 없으면 자동으로 skip됩니다.

## API 레퍼런스

### 기능1. 음성 일지

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/stt` | 음성 파일(`multipart/form-data`) → STT 텍스트 반환 |
| POST | `/api/visit-logs` | STT 텍스트 → 구조화 방문일지 생성·저장 |
| GET | `/api/visit-logs/{elderId}` | 어르신별 방문일지 목록 조회 |

### 기능2. 경력 포트폴리오

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/portfolio/{workerId}` | SW 경력 포트폴리오(통계+타임라인) 조회 |

### 기능3. 인수인계 카드

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/handover-cards/{elderId}/generate` | 방문일지 기반 카드 초안 생성 |
| PUT | `/api/handover-cards/{cardId}/confirm` | 검토·수정 후 확정 |
| GET | `/api/handover-cards/{elderId}/latest` | 최신 카드 조회 |
| GET | `/api/handover-cards/{elderId}/history` | 버전 이력 조회 |
| GET | `/api/handover-cards/{cardId}/source-logs` | 카드 근거가 된 원본 로그 목록 (추적용) |

### 개발용

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/dev/seed` | 더미 데이터 시딩 (어르신 3명 + 확정 방문일지 20건, 멱등) |

> 시더로 생성되는 어르신: `elder-001`(김순자), `elder-002`(박철수), `elder-003`(이복례)

## Firestore 데이터 모델

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
  - elderId, workerId, visitDateTime
  - rawSttText                 // STT 원문
  - structuredLog: {
      serviceType,              // 안전지원 / 사회참여 / 일상생활지원
      activityDetail, elderCondition, specialNote
    }
  - riskTags: [string]
  - status: "draft" | "confirmed"
  - confirmedBy, confirmedAt

handoverCards (인수인계 카드) — 기능3 산출물, 버전별 이력 보존
  - cardId (doc id)
  - elderId, generatedAt
  - previousWorkerId, newWorkerId
  - sourceLogRange: { fromDate, toDate, logCount }
  - sourceLogIds: [logId, ...]
  - summary: {
      basicInfo: { livingCondition, familyRelation, chronicConditions, medications },
      personality,
      emotionalTriggers: [{ trigger, description, sourceLogIds }],
      preferredTopics, avoidTopics,
      recentThreeMonthSummary
    }
  - version, previousVersionId
  - status: "draft" | "confirmed"
  - confirmedBy, confirmedAt
```

전체 스키마와 각 기능의 상세 명세는 [`CLAUDE.md`](./CLAUDE.md)를 참고하세요.

## 설계 원칙

- **환각 방지**: LLM 출력에서 원본 텍스트에 없는 사실은 추정하지 않습니다. 애매하면 필드를 비우고 사람이 확인하게 합니다.
- **근거 추적 가능성**: 인수인계 카드의 모든 판단(특히 정서 트리거)에는 근거가 된 방문일지 `logId`를 명시합니다.
- **버전 보존**: 인수인계 카드는 재생성 시 이전 버전을 삭제하지 않고 `previousVersionId`로 연결해 이력을 보존합니다.
- **접근 제어**: SW는 자신에게 배정된 어르신 문서만 read/write, ADMIN은 소속 기관 전체를 read하며 write는 확정(confirm) 액션에 한정합니다.
- **민감정보 보호**: `elders`, `handoverCards` 등 건강·정서 정보를 포함하는 컬렉션은 접근 로그를 별도로 기록하고, 음성 원본은 STT 완료 후 삭제(설정 가능)합니다.

## 팀

| 기능 | 우선순위 | 담당 |
|---|---|---|
| 공통 ClaudeService | 최우선(선행) | 박수지 |
| 기능1. 음성 일지 서식화 | MUST | 박수지 |
| 기능2. 경력 포트폴리오 | SHOULD | 정현민 |
| 기능3. 인수인계 카드 | SHOULD | 신승민 |
| PHQ-9 심화조사지 | 스트레치 | 시간 되는 사람 / 신승민 |

---

더 자세한 기능별 스펙(프롬프트 설계 원칙, 완료 기준 등)은 [`CLAUDE.md`](./CLAUDE.md)에, 백엔드 세부 실행 가이드는 [`backend/README.md`](./backend/README.md)에 정리되어 있습니다.
