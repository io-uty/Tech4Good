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

export async function submitStt(audioFile: Blob | File): Promise<{ rawText: string }> {
  try {
    const formData = new FormData();
    formData.append("audioFile", audioFile);

    const res = await fetch(`${API_BASE}/stt`, {
      method: "POST",
      body: formData,
    });
    if (!res.ok) throw new Error("Failed to convert speech to text");
    return await res.json();
  } catch (e) {
    console.warn("Mocking STT response:", e);
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

export async function getHandover(elderId: string): Promise<HandoverResponse> {
  try {
    const res = await fetch(`${API_BASE}/handover/${elderId}`);
    if (!res.ok) throw new Error("Failed to fetch handover data");
    return await res.json();
  } catch (e) {
    console.warn("Mocking Handover response:", e);
    return {
      elderId: elderId,
      name: "김순이",
      careYears: "3년째 돌봄",
      tips: [
        { text: "귀가 어두우시니 낮은 톤으로 천천히 말씀해 주세요.", caution: false },
        { text: "트로트 가수 임영웅 노래를 틀어드리면 마음을 쉽게 여심.", caution: false },
        { text: "5월에는 사별한 배우자 생각으로 우울증이 깊어지니 정서 지원을 늘려주세요.", caution: true }
      ]
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
