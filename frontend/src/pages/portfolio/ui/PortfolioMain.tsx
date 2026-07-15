import React, { useState } from "react";
import { Bell, Download, User, Clock, Users, CalendarCheck, ShieldCheck, HeartPulse, Pill, Utensils, Stethoscope, AlertTriangle, BadgeCheck, MessageSquare, Star } from "lucide-react";
import { PortfolioResponse } from "../../../shared/types";
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid, LineChart, Line } from "recharts";

type PortfolioMainProps = {
  data: PortfolioResponse;
  onDownload: () => void;
};

export function PortfolioMain({ data, onDownload }: PortfolioMainProps) {
  const [trendTab, setTrendTab] = useState<"visits" | "hours" | "elders">("visits");

  const trendDataKey = trendTab === "visits" ? "visits" : trendTab === "hours" ? "hours" : "elders";

  const renderTimelineIcon = (iconType: string) => {
    switch (iconType) {
      case "badge": return <BadgeCheck size={18} />;
      case "user": return <User size={18} />;
      case "chat": return <MessageSquare size={18} />;
      case "hospital": return <Stethoscope size={18} />;
      case "star": return <Star size={18} />;
      default: return <User size={18} />;
    }
  };

  const renderCareIcon = (iconType: string) => {
    switch (iconType) {
      case "emotion": return <HeartPulse size={24} className="text-[#FF7F8E]" />;
      case "medicine": return <Pill size={24} className="text-[#4A90E2]" />;
      case "food": return <Utensils size={24} className="text-[#64B5F6]" />;
      case "hospital": return <Stethoscope size={24} className="text-[#F44336]" />;
      case "emergency": return <AlertTriangle size={24} className="text-[#E53935]" />;
      default: return <HeartPulse size={24} />;
    }
  };

  return (
    <div className="flex-1 overflow-y-auto bg-[#F7F5F0] pb-8 relative">
      {/* Header */}
      <header className="flex justify-between items-center p-5 pt-6 sticky top-0 bg-[#F7F5F0]/90 backdrop-blur-sm z-50">
        <div className="text-[22px] font-extrabold text-[#89BAB1] tracking-tight">포트폴리오</div>
      </header>

      {/* 0. 프로필 카드 섹션 */}
      <div className="px-4 pt-1 mb-2">
        <div className="bg-gradient-to-br from-[#89BAB1] to-[#1E3E35] rounded-xl p-5 text-white shadow-sm relative overflow-hidden">
          <div className="flex items-center gap-4 relative z-10">
            <div className="flex-1">
              <div className="flex items-end gap-1.5 mb-1">
                <h1 className="text-[20px] font-bold leading-tight">박복자</h1>
                <span className="text-[14px] text-white/90">생활지원사</span>
              </div>
              <div className="text-[13px] text-white/90 mb-4">행복돌봄센터</div>

              <div className="flex items-center gap-4">
                <div className="flex flex-col items-start">
                  <span className="bg-white/15 px-2 py-1 rounded-lg text-[11px] text-white/90 mb-1">생활지원사 경력</span>
                  <span className="text-[15px] font-bold">2년 3개월</span>
                </div>

                <div className="w-[1px] h-10 bg-white/30 shrink-0"></div>

                <div className="flex flex-col items-start">
                  <span className="bg-white/15 px-2 py-1 rounded-lg text-[11px] text-white/90 mb-1">현 기관 근무기간</span>
                  <span className="text-[15px] font-bold">2026.01 ~ 2026.12</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 1. 상단 통계 4종 (Grid) */}
      <div className="grid grid-cols-2 gap-3 p-4">
        <div className="bg-white rounded-xl p-4 shadow-sm border border-[#EBE8E0] flex flex-col items-center justify-center">
          <div className="w-10 h-10 rounded-full bg-[#E3F2ED] text-[#89BAB1] flex items-center justify-center mb-2">
            <User size={20} />
          </div>
          <div className="text-[12px] text-[#6E756A] font-medium">총 방문 수</div>
          <div className="text-[20px] font-bold text-[#2B2E28]">{data.stats.totalCheckins.toLocaleString()}회</div>
        </div>
        <div className="bg-white rounded-xl p-4 shadow-sm border border-[#EBE8E0] flex flex-col items-center justify-center">
          <div className="w-10 h-10 rounded-full bg-[#FFF3E0] text-[#E68A00] flex items-center justify-center mb-2">
            <Clock size={20} />
          </div>
          <div className="text-[12px] text-[#6E756A] font-medium">총 돌봄 시간</div>
          <div className="text-[20px] font-bold text-[#2B2E28]">{data.stats.totalHours.toLocaleString()}시간</div>
        </div>
        <div className="bg-white rounded-xl p-4 shadow-sm border border-[#EBE8E0] flex flex-col items-center justify-center">
          <div className="w-10 h-10 rounded-full bg-[#E8F5E9] text-[#4CAF50] flex items-center justify-center mb-2">
            <Users size={20} />
          </div>
          <div className="text-[12px] text-[#6E756A] font-medium">담당 어르신</div>
          <div className="text-[20px] font-bold text-[#2B2E28]">{data.stats.elderCount}명</div>
        </div>
        <div className="bg-white rounded-xl p-4 shadow-sm border border-[#EBE8E0] flex flex-col items-center justify-center">
          <div className="w-10 h-10 rounded-full bg-[#E3F2FD] text-[#2196F3] flex items-center justify-center mb-2">
            <CalendarCheck size={20} />
          </div>
          <div className="text-[12px] text-[#6E756A] font-medium">출근율</div>
          <div className="text-[20px] font-bold text-[#2B2E28]">{data.stats.attendanceRate}%</div>
        </div>
      </div>

      {/* 2. 근태 현황 */}
      <div className="px-4 mb-4">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-[#EBE8E0]">
          <div className="flex items-center gap-2 mb-4">
            <CalendarCheck size={18} className="text-[#89BAB1]" />
            <h2 className="text-[16px] font-bold text-[#2B2E28]">근태 현황 <span className="text-[13px] text-[#A4A9A0] font-normal">(2026.01 ~ 2026.12)</span></h2>
          </div>

          <div className="flex items-center mb-6">
            <div className="w-[110px] h-[110px] relative shrink-0">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={[{ value: data.stats.attendanceRate }, { value: 100 - data.stats.attendanceRate }]}
                    cx="50%" cy="50%"
                    innerRadius={38} outerRadius={48}
                    startAngle={90} endAngle={-270}
                    dataKey="value"
                    stroke="none"
                  >
                    <Cell fill="#89BAB1" />
                    <Cell fill="#F0F0F0" />
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
              <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                <span className="text-[24px] font-bold text-[#2B2E28]">{data.stats.attendanceRate}%</span>
                <span className="text-[11px] text-[#6E756A]">출근율</span>
              </div>
            </div>

            <div className="flex-1 grid grid-cols-2 gap-y-3 pl-4">
              <div>
                <div className="text-[11px] text-[#6E756A] mb-0.5 flex items-center gap-1"><CalendarCheck size={12} className="text-[#89BAB1]" /> 총 근무일</div>
                <div className="text-[15px] font-bold text-[#2B2E28]">{data.attendanceStats.totalWorkDays}일</div>
              </div>
              <div>
                <div className="text-[11px] text-[#6E756A] mb-0.5 flex items-center gap-1"><span className="text-[#E53935] text-[12px]">✕</span> 결근</div>
                <div className="text-[15px] font-bold text-[#2B2E28]">{data.attendanceStats.absence}일</div>
              </div>
              <div>
                <div className="text-[11px] text-[#6E756A] mb-0.5 flex items-center gap-1"><Clock size={12} className="text-[#E68A00]" /> 지각</div>
                <div className="text-[15px] font-bold text-[#2B2E28]">{data.attendanceStats.late}회</div>
              </div>
              <div>
                <div className="text-[11px] text-[#6E756A] mb-0.5 flex items-center gap-1"><BadgeCheck size={12} className="text-[#89BAB1]" /> 일지 완료율</div>
                <div className="text-[15px] font-bold text-[#2B2E28]">{data.attendanceStats.logCompletionRate}%</div>
              </div>
            </div>
          </div>

          <div className="h-[120px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.attendanceMonthly} margin={{ top: 0, right: 0, left: -25, bottom: 0 }} barSize={6}>
                <XAxis dataKey="month" axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#A4A9A0' }} dy={10} />
                <Tooltip cursor={{ fill: '#F5F5F5' }} contentStyle={{ fontSize: '12px', borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                <Bar dataKey="attendance" stackId="a" fill="#89BAB1" radius={[0, 0, 4, 4]} />
                <Bar dataKey="late" stackId="a" fill="#E68A00" />
                <Bar dataKey="absence" stackId="a" fill="#E53935" />
                <Bar dataKey="vacation" stackId="a" fill="#E0E0E0" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
          <div className="flex justify-center gap-3 mt-2 text-[10px] text-[#6E756A]">
            <span className="flex items-center gap-1"><div className="w-2 h-2 bg-[#89BAB1] rounded-[2px]" /> 출근</span>
            <span className="flex items-center gap-1"><div className="w-2 h-2 bg-[#E68A00] rounded-[2px]" /> 지각</span>
            <span className="flex items-center gap-1"><div className="w-2 h-2 bg-[#E53935] rounded-[2px]" /> 결근</span>
            <span className="flex items-center gap-1"><div className="w-2 h-2 bg-[#E0E0E0] rounded-[2px]" /> 휴무</span>
          </div>
        </div>
      </div>

      {/* 3. 활동 추이 (월별) */}
      <div className="px-4 mb-4">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-[#EBE8E0]">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <ShieldCheck size={18} className="text-[#89BAB1]" />
              <h2 className="text-[16px] font-bold text-[#2B2E28]">활동 추이 <span className="text-[13px] text-[#A4A9A0] font-normal">(월별)</span></h2>
            </div>
          </div>

          <div className="flex gap-2 mb-6 overflow-x-auto pb-1 scrollbar-hide">
            <button onClick={() => setTrendTab("visits")} className={`shrink-0 px-3 py-1.5 rounded-full text-[13px] font-bold border transition-colors ${trendTab === "visits" ? "bg-[#F0F7F4] text-[#89BAB1] border-[#89BAB1]" : "bg-white text-[#6E756A] border-[#EBE8E0]"}`}>방문 횟수</button>
            <button onClick={() => setTrendTab("hours")} className={`shrink-0 px-3 py-1.5 rounded-full text-[13px] font-bold border transition-colors ${trendTab === "hours" ? "bg-[#F0F7F4] text-[#89BAB1] border-[#89BAB1]" : "bg-white text-[#6E756A] border-[#EBE8E0]"}`}>돌봄 시간</button>
            <button onClick={() => setTrendTab("elders")} className={`shrink-0 px-3 py-1.5 rounded-full text-[13px] font-bold border transition-colors ${trendTab === "elders" ? "bg-[#F0F7F4] text-[#89BAB1] border-[#89BAB1]" : "bg-white text-[#6E756A] border-[#EBE8E0]"}`}>담당 어르신 수</button>
          </div>

          <div className="h-[180px] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data.activityTrends} margin={{ top: 20, right: 10, left: -25, bottom: 0 }}>
                <CartesianGrid vertical={false} stroke="#F0F0F0" />
                <XAxis dataKey="month" axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#A4A9A0' }} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 10, fill: '#A4A9A0' }} />
                <Tooltip contentStyle={{ fontSize: '12px', borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                <Line type="monotone" dataKey={trendDataKey} stroke="#89BAB1" strokeWidth={2} dot={{ r: 4, fill: "#fff", stroke: "#89BAB1", strokeWidth: 2 }} activeDot={{ r: 6, fill: "#89BAB1", strokeWidth: 0 }} label={{ position: 'top', fill: '#2B2E28', fontSize: 10, dy: -5 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* 4. 주요 돌봄 수행 실적 */}
      <div className="px-4 mb-4">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-[#EBE8E0]">
          <div className="flex items-center gap-2 mb-4">
            <BadgeCheck size={18} className="text-[#89BAB1]" />
            <h2 className="text-[16px] font-bold text-[#2B2E28]">주요 돌봄 수행 실적 <span className="text-[13px] text-[#A4A9A0] font-normal">(1년)</span></h2>
          </div>

          <div className="flex gap-3 overflow-x-auto pb-2 scrollbar-hide snap-x">
            {data.carePerformances.map((perf) => (
              <div key={perf.id} className="snap-start shrink-0 w-[110px] bg-white rounded-xl p-4 border border-[#F0F0F0] shadow-[0_2px_8px_rgba(0,0,0,0.03)] flex flex-col items-center text-center">
                <div className="w-12 h-12 rounded-full bg-[#F7F5F0] flex items-center justify-center mb-3">
                  {renderCareIcon(perf.iconType)}
                </div>
                <div className="text-[12px] text-[#6E756A] font-medium leading-snug mb-1 whitespace-pre-wrap">{perf.label}</div>
                <div className="text-[16px] font-bold text-[#2B2E28] mt-auto">{perf.value}</div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 5. 경력 및 자격 */}
      <div className="px-4 mb-4">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-[#EBE8E0] flex flex-col gap-6">
          <div>
            <div className="flex items-center gap-2 mb-4">
              <CalendarCheck size={18} className="text-[#89BAB1]" />
              <h2 className="text-[16px] font-bold text-[#2B2E28]">경력 및 자격</h2>
            </div>

            <div className="text-[12px] text-[#A4A9A0] mb-2 font-bold">경력</div>
            <div className="bg-[#FBFBFA] rounded-xl p-3 border border-[#F0F0F0] flex flex-col gap-3">
              {data.experiences.map((exp, i) => (
                <div key={i} className="flex flex-col sm:flex-row sm:items-center gap-1 sm:gap-3 text-[13px]">
                  <div className="text-[#A4A9A0] shrink-0 sm:w-[110px]">{exp.period}</div>
                  <div className="flex items-start sm:items-center gap-1.5 flex-1">
                    <div className={`w-1.5 h-1.5 rounded-full mt-1.5 sm:mt-0 shrink-0 ${exp.isActive ? "bg-[#89BAB1]" : "bg-[#E0E0E0]"}`}></div>
                    <span className={`font-bold leading-tight break-keep ${exp.isActive ? "text-[#89BAB1]" : "text-[#6E756A]"}`}>{exp.title}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div>
            <div className="text-[12px] text-[#A4A9A0] mb-2 font-bold mt-2 md:mt-0">보유 자격증</div>
            <div className="flex flex-col gap-3">
              {data.certificates.map((cert, i) => (
                <div key={i} className="flex items-center justify-between text-[13px]">
                  <div className="flex items-center gap-2">
                    <BadgeCheck size={16} className="text-[#4CAF50] shrink-0" />
                    <span className="font-medium text-[#2B2E28] break-keep">{cert.title}</span>
                  </div>
                  <span className="text-[#A4A9A0] shrink-0 ml-2">{cert.date}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* 6. 경력 타임라인 */}
      <div className="px-4 mb-4">
        <div className="bg-white rounded-xl p-5 shadow-sm border border-[#EBE8E0] overflow-hidden">
          <div className="flex items-center gap-2 mb-6">
            <CalendarCheck size={18} className="text-[#89BAB1]" />
            <h2 className="text-[16px] font-bold text-[#2B2E28]">경력 타임라인</h2>
          </div>

          <div className="overflow-x-auto scrollbar-hide pb-4 -mx-5 px-5">
            <div className="flex min-w-max relative -ml-3">
              <div className="absolute top-5 left-10 right-10 h-1 bg-[#89BAB1] rounded-full"></div>

              {data.timeline.map((item, i) => (
                <div key={i} className="flex flex-col items-center w-[110px] relative z-10 shrink-0">
                  <div className="w-10 h-10 rounded-full bg-white border-[3px] border-[#89BAB1] text-[#89BAB1] flex items-center justify-center mb-3">
                    {renderTimelineIcon(item.iconType)}
                  </div>
                  <div className="text-[12px] font-bold text-[#6E756A] mb-1">{item.date}</div>
                  <div className="text-[13px] font-bold text-[#2B2E28] text-center leading-tight">{item.title}</div>
                  <div className="text-[12px] text-[#6E756A] text-center">{item.subtitle}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      <div className="px-4">
        <button className="bg-[#89BAB1] text-white border-none rounded-xl p-4 text-[17px] font-bold cursor-pointer w-full flex items-center justify-center gap-2 shadow-sm" onClick={onDownload}>
          <Download size={18} />
          포트폴리오 PDF 다운로드
        </button>
      </div>
    </div>
  );
}
