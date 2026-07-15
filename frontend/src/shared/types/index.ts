import React from "react";

export type ResultCardType = {
  key: string;
  label: string;
  icon: React.ElementType;
  text: string;
};

export type TipType = {
  text: string;
  caution: boolean;
};

export type HandoverType = {
  name: string;
  years: string;
  tips: TipType[];
};

export type StatType = {
  label: string;
  value: string;
  unit: string;
};

export type TimelineType = {
  date: string;
  text: string;
};

export type PortfolioType = {
  stats: StatType[];
  timeline: TimelineType[];
};

export type VisitLogResponse = {
  logId: string;
  elderId: string;
  workerId: string;
  createdAt: string;
  body: string;
  food: string;
  emotion: string;
  cognition: string;
  journalEntry: string;
};

export type HandoverBasicInfoType = {
  livingCondition: string;
  familyRelation: string;
  chronicConditions: string[];
  medications: string[];
};

export type EmotionalTriggerType = {
  trigger: string;
  description: string;
  sourceLogIds: string[];
};

export type HandoverSummaryType = {
  basicInfo: HandoverBasicInfoType;
  personality: string;
  emotionalTriggers: EmotionalTriggerType[];
  preferredTopics: string[];
  avoidTopics: string[];
  recentThreeMonthSummary: string;
};

// 기능3 인수인계 카드 (CLAUDE.md 8.1절) — 백엔드 HandoverCard와 동일한 구조
export type HandoverResponse = {
  cardId: string;
  elderId: string;
  generatedAt: string;
  previousWorkerId: string | null;
  newWorkerId: string | null;
  sourceLogRange: { fromDate: string; toDate: string; logCount: number };
  sourceLogIds: string[];
  summary: HandoverSummaryType;
  version: number;
  previousVersionId: string | null;
  status: string;
  confirmedBy: string | null;
  confirmedAt: string | null;
};

export type AssignedElderType = {
  elderId: string;
  name: string;
  age: number;
  gender: string;
  address: string;
  image?: string;
};

export type LinkedServiceType = {
  serviceName: string;
  provider: string;
  period: string;
  description: string;
};

export type ElderProfileType = {
  elderId: string;
  birthDate: string;
  address: string;
  family: string;
  health: string;
  governmentSupport: string[];
  linkedServices: LinkedServiceType[];
};

export type CareByElderType = {
  elderId: string;
  elderName: string;
  period: string;
  summary: string;
};

export type AttendanceType = {
  name: string; // 출근, 지각, 무단 지각, 무단 결근
  value: number; // 일수
};

export type ElderImprovementType = {
  month: string;
  score: number;
};

export type AttendanceMonthlyType = {
  month: string;
  attendance: number; // 출근 일수
  late: number; // 지각 일수
  absence: number; // 결근 일수
  vacation: number; // 휴무 일수
};

export type ActivityTrendType = {
  month: string;
  visits: number;
  hours: number;
  elders: number;
};

export type CarePerformanceType = {
  id: string;
  label: string;
  value: string;
  iconType: "emotion" | "medicine" | "food" | "hospital" | "emergency";
};

export type ExperienceType = {
  period: string;
  title: string;
  isActive: boolean;
};

export type CertificateType = {
  title: string;
  date: string;
};

export type DashboardTimelineType = {
  date: string;
  title: string;
  subtitle: string;
  iconType: "badge" | "user" | "chat" | "hospital" | "star";
};

export type PortfolioResponse = {
  workerId: string;
  stats: {
    totalCheckins: number;
    totalHours: number;
    elderCount: number;
    attendanceRate: number;
  };
  attendanceStats: {
    totalWorkDays: number;
    absence: number;
    late: number;
    logCompletionRate: number;
  };
  attendanceMonthly: AttendanceMonthlyType[];
  activityTrends: ActivityTrendType[];
  carePerformances: CarePerformanceType[];
  experiences: ExperienceType[];
  certificates: CertificateType[];
  timeline: DashboardTimelineType[];
};
