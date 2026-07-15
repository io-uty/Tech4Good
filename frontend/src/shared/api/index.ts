import { HandoverResponse, PortfolioResponse, VisitLogResponse, AssignedElderType, ElderProfileType } from "../types";

const API_BASE = "/api";

export async function getAssignedElders(workerId: string): Promise<AssignedElderType[]> {
  // Mock function for getting the list of assigned elders
  return [
    {
      elderId: "elder-1",
      name: "김순이",
      age: 82,
      gender: "여",
      address: "서울특별시 성북구 삼선교로",
    },
    {
      elderId: "elder-2",
      name: "박정자",
      age: 78,
      gender: "여",
      address: "서울특별시 성북구 동소문로",
    },
    {
      elderId: "elder-3",
      name: "이철수",
      age: 85,
      gender: "남",
      address: "서울특별시 성북구 보문로",
    }
  ];
}

export async function getElderProfile(elderId: string): Promise<ElderProfileType> {
  // Mock function for getting elder demographic info and linked services
  return {
    elderId,
    birthDate: "1944.05.12",
    address: "서울특별시 성북구 삼선교로 16길 12, 1층",
    family: "독거 (아들 1명 있으나 연락 두절)",
    health: "고혈압, 관절염 (매월 1회 안암병원 정기진료)",
    governmentSupport: [
      "노인장기요양보험 4등급",
      "기초연금 수급자",
      "보건소 방문간호 서비스 (월 1회)"
    ],
    linkedServices: [
      {
        serviceName: "어르신 도시락 배달 서비스",
        provider: "성북구청 노인복지과",
        period: "2023.01 ~ 현재",
        description: "주 3회 (월, 수, 금) 점심 도시락 배달 및 안부 확인"
      },
      {
        serviceName: "주거환경 개선 지원",
        provider: "성북종합사회복지관",
        period: "2023.11",
        description: "화장실 미끄럼 방지 매트 및 안전 손잡이 설치"
      }
    ]
  };
}

function guessAudioExtension(mimeType: string): string {
  if (mimeType.includes("webm")) return "webm";
  if (mimeType.includes("ogg")) return "ogg";
  if (mimeType.includes("mp4") || mimeType.includes("aac")) return "m4a";
  if (mimeType.includes("wav")) return "wav";
  return "webm";
}

export async function submitStt(audioFile: Blob | File): Promise<{ rawText: string }> {
  try {
    const formData = new FormData();
    const filename = audioFile instanceof File
      ? audioFile.name
      : `recording.${guessAudioExtension(audioFile.type)}`;
    formData.append("audioFile", audioFile, filename);

    const res = await fetch(`${API_BASE}/stt`, {
      method: "POST",
      body: formData,
    });
    if (!res.ok) {
      const errorBody = await res.text().catch(() => "(응답 본문 읽기 실패)");
      throw new Error(`STT 실패 (status=${res.status}): ${errorBody}`);
    }
    const json = await res.json();
    if (!json.rawText) {
      // Clova가 200을 줬지만 text가 빈 값인 경우 (침묵 구간 등) — 목업으로 조용히 덮지 않고 그대로 노출
      console.warn("STT 응답에 rawText가 비어 있음:", json);
    }
    return json;
  } catch (e) {
    // 실제 원인을 반드시 콘솔에서 확인할 것 (네트워크 에러 / 5xx / Clova 키 문제 등)
    console.error("STT 호출 실패, 목업으로 대체:", e);
    return {
      rawText: "오늘 김순이 어르신 방문했는데 컨디션은 괜찮으신데 점심을 반 정도밖에 안 드셨고 아드님 얘기 나오니까 말수가 줄었어요"
    };
  }
}

export async function submitVisitLog(workerId: string, elderId: string, rawText: string): Promise<VisitLogResponse> {
  try {
    const res = await fetch(`${API_BASE}/visit-logs`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ workerId, elderId, rawText }),
    });
    if (!res.ok) throw new Error("Failed to submit visit log");
    return await res.json();
  } catch (e) {
    console.warn("Mocking VisitLog response:", e);
    return {
      logId: "log_20260715_001",
      elderId: elderId,
      workerId: workerId,
      createdAt: new Date().toISOString(),
      body: "특이사항 없음, 컨디션 양호",
      food: "점심 절반 정도 드심, 식사 독려함",
      emotion: "아드님 이야기 나오자 말수 줄어듦",
      cognition: "대화 흐름 자연스러움",
      journalEntry: "오늘 방문 시 전반적인 컨디션은 양호하셨으나 점심 식사량이 평소보다 적었다. 아드님 이야기가 나오자 말수가 줄어드는 모습을 보이셨는데, 가족 관련 화제는 조심스럽게 접근할 필요가 있어 보인다."
    };
  }
}

export async function getVisitLogs(elderId: string): Promise<VisitLogResponse[]> {
  try {
    const res = await fetch(`${API_BASE}/visit-logs/${elderId}`);
    if (!res.ok) throw new Error("Failed to fetch visit logs");
    return await res.json();
  } catch (e) {
    console.warn("Mocking VisitLogs response:", e);
    return [
      {
        logId: "log_20260710_001",
        elderId,
        workerId: "worker-1",
        createdAt: "2026-07-10T10:30:00+09:00",
        body: "특이사항 없음, 컨디션 양호",
        food: "점심 절반 정도 드심",
        emotion: "아드님 이야기 나오자 말수 줄어듦",
        cognition: "대화 흐름 자연스러움",
        journalEntry: "전반적인 컨디션은 양호하셨으나 점심 식사량이 평소보다 적었다. 아드님 이야기가 나오자 말수가 줄어드는 모습을 보이셨는데, 가족 관련 화제는 조심스럽게 접근할 필요가 있어 보인다."
      },
      {
        logId: "log_20260703_001",
        elderId,
        workerId: "worker-1",
        createdAt: "2026-07-03T11:00:00+09:00",
        body: "혈압 140/90, 걸음걸이 안정적",
        food: "죽 반 그릇, 입맛 없음",
        emotion: "표정 어두움, 말수 줄어듦",
        cognition: "날짜·요일 정확히 기억",
        journalEntry: "혈압이 평소보다 다소 높게 측정되어 안정을 권해드렸다. 식사량이 줄어 식욕 저하가 우려되며, 정서적으로도 위축된 모습을 보이셨다."
      },
      {
        logId: "log_20260626_001",
        elderId,
        workerId: "worker-1",
        createdAt: "2026-06-26T09:45:00+09:00",
        body: "무릎 통증 호소",
        food: "아침 정상 섭취",
        emotion: "",
        cognition: "",
        journalEntry: "무릎 통증을 호소하셔서 병원 진료를 권해드렸다. 그 외 특이사항은 없었다."
      }
    ];
  }
}

export async function getHandover(elderId: string): Promise<HandoverResponse> {
  try {
    const res = await fetch(`${API_BASE}/handover-cards/${elderId}/latest`);
    if (!res.ok) throw new Error("Failed to fetch handover data");
    return await res.json();
  } catch (e) {
    console.warn("Mocking Handover response:", e);
    return {
      cardId: "card_mock_001",
      elderId: elderId,
      generatedAt: "2026-07-10T09:00:00+09:00",
      previousWorkerId: null,
      newWorkerId: "worker-1",
      sourceLogRange: { fromDate: "2026-05-01", toDate: "2026-07-10", logCount: 6 },
      sourceLogIds: ["log_20260710_001", "log_20260703_001", "log_20260626_001"],
      summary: {
        basicInfo: {
          livingCondition: "독거, 반지하 1층 거주",
          familyRelation: "아들 1명 있으나 왕래 거의 없음",
          chronicConditions: ["고혈압"],
          medications: ["혈압약(아침 1회)"]
        },
        personality: "귀가 어두워 낮은 톤으로 천천히 말씀드려야 하며, 트로트 노래를 틀어드리면 마음을 쉽게 여시는 편이다.",
        emotionalTriggers: [
          {
            trigger: "아드님 이야기가 나오면 말수가 줄어듦",
            description: "가족 관련 화제는 조심스럽게 접근하고, 화제를 전환할 준비를 해두는 것이 좋다.",
            sourceLogIds: ["log_20260710_001", "log_20260703_001"]
          }
        ],
        preferredTopics: ["트로트 음악", "옛날 동네 이야기"],
        avoidTopics: ["아드님/가족 관계 — 말수가 줄고 표정이 어두워짐"],
        recentThreeMonthSummary: "전반적인 컨디션은 양호하나 식사량이 다소 줄었고, 혈압이 평소보다 높게 측정된 날이 있어 주의가 필요하다."
      },
      version: 1,
      previousVersionId: null,
      status: "confirmed",
      confirmedBy: "worker-1",
      confirmedAt: "2026-07-10T09:00:00+09:00"
    };
  }
}

export async function getPortfolio(workerId: string): Promise<PortfolioResponse> {
  try {
    const res = await fetch(`${API_BASE}/portfolio/${workerId}`);
    if (!res.ok) throw new Error("Failed to fetch portfolio data");
    return await res.json();
  } catch (e) {
    console.warn("Mocking Portfolio response:", e);
    return {
      workerId: workerId,
      stats: { totalCheckins: 1248, totalHours: 1872, elderCount: 18, attendanceRate: 98 },
      attendanceStats: { totalWorkDays: 245, absence: 2, late: 1, logCompletionRate: 97 },
      attendanceMonthly: [
        { month: "1월", attendance: 20, late: 0, absence: 0, vacation: 2 },
        { month: "2월", attendance: 18, late: 1, absence: 0, vacation: 1 },
        { month: "3월", attendance: 22, late: 0, absence: 0, vacation: 0 },
        { month: "4월", attendance: 21, late: 0, absence: 1, vacation: 0 },
        { month: "5월", attendance: 20, late: 0, absence: 0, vacation: 2 },
        { month: "6월", attendance: 21, late: 0, absence: 0, vacation: 1 },
        { month: "7월", attendance: 22, late: 0, absence: 0, vacation: 0 },
        { month: "8월", attendance: 20, late: 0, absence: 1, vacation: 1 },
        { month: "9월", attendance: 21, late: 0, absence: 0, vacation: 1 },
        { month: "10월", attendance: 22, late: 0, absence: 0, vacation: 0 },
        { month: "11월", attendance: 21, late: 0, absence: 0, vacation: 1 },
        { month: "12월", attendance: 17, late: 0, absence: 0, vacation: 5 },
      ],
      activityTrends: [
        { month: "1월", visits: 96, hours: 144, elders: 14 },
        { month: "2월", visits: 101, hours: 151, elders: 15 },
        { month: "3월", visits: 98, hours: 147, elders: 15 },
        { month: "4월", visits: 105, hours: 157, elders: 16 },
        { month: "5월", visits: 108, hours: 162, elders: 16 },
        { month: "6월", visits: 102, hours: 153, elders: 16 },
        { month: "7월", visits: 99, hours: 148, elders: 17 },
        { month: "8월", visits: 104, hours: 156, elders: 17 },
        { month: "9월", visits: 103, hours: 154, elders: 17 },
        { month: "10월", visits: 107, hours: 160, elders: 18 },
        { month: "11월", visits: 109, hours: 163, elders: 18 },
        { month: "12월", visits: 116, hours: 174, elders: 18 },
      ],
      carePerformances: [
        { id: "p1", label: "정서 지원", value: "842회", iconType: "emotion" },
        { id: "p2", label: "복약 확인", value: "623회", iconType: "medicine" },
        { id: "p3", label: "식사·생활\n상태 확인", value: "1,011회", iconType: "food" },
        { id: "p4", label: "병원 동행 및\n서비스 연계", value: "51건", iconType: "hospital" },
        { id: "p5", label: "응급상황\n보고·대응", value: "8건", iconType: "emergency" }
      ],
      experiences: [
        { period: "2024.09 ~ 2024.12", title: "OO돌봄센터 근무", isActive: false },
        { period: "2025.01 ~ 2025.12", title: "OO노인복지관 근무", isActive: false },
        { period: "2026.01 ~ 2026.12", title: "행복돌봄센터 근무 (현재)", isActive: true },
      ],
      certificates: [
        { title: "생활지원사 교육 수료", date: "2023.08" },
        { title: "요양보호사 1급", date: "2023.06" },
        { title: "사회복지사 2급", date: "2022.02" },
      ],
      timeline: [
        { date: "2024.09", title: "생활지원사", subtitle: "근무 시작", iconType: "badge" },
        { date: "2025.03", title: "담당 어르신", subtitle: "10명 달성", iconType: "user" },
        { date: "2025.08", title: "누적 방문", subtitle: "500회 달성", iconType: "chat" },
        { date: "2026.04", title: "병원·복지서비스", subtitle: "연계 30건 달성", iconType: "hospital" },
        { date: "2026.12", title: "총 방문 1,248회", subtitle: "달성", iconType: "star" },
      ]
    };
  }
}
