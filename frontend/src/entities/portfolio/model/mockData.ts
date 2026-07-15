import { PortfolioType } from "../../../shared/types";

export const PORTFOLIO: PortfolioType = {
  stats: [
    { label: "누적 안부 확인", value: "1,200", unit: "회" },
    { label: "누적 돌봄 시간", value: "1,800", unit: "시간" },
    { label: "담당 어르신", value: "16", unit: "명" },
  ],
  timeline: [
    { date: "2026.03", text: "정서적 안정 기여 3건 (대화 키워드 변화율 개선 확인)" },
    { date: "2025.11", text: "낙상 위험 신호 조기 포착, 선제 예방 조치 5건" },
    { date: "2025.06", text: "우울 위험군 어르신 정서 지원 프로그램 연계 2건" },
  ],
};
