import React, { useState, useMemo } from "react";
import { Bell, ChevronRight, Clock, MapPin } from "lucide-react";
import logo from "../../../logo.webp";

const generateDates = () => {
  const dates = [];
  const today = new Date();
  const days = ["일", "월", "화", "수", "목", "금", "토"];

  for (let i = -2; i <= 4; i++) {
    const date = new Date(today);
    date.setDate(today.getDate() + i);
    dates.push({
      date: date.getDate(),
      day: days[date.getDay()],
      fullDate: date.toISOString().split("T")[0],
      isToday: i === 0,
      diff: i,
    });
  }
  return dates;
};

// 가짜 데이터 생성 함수: 날짜별로 다른 어르신 목록을 반환
const generateMockDataForDates = (dates: any[]) => {
  const data: Record<string, any[]> = {};
  dates.forEach(d => {
    if (d.isToday) {
      data[d.fullDate] = [
        { id: 1, time: "10:00", name: "김말복", title: "김말복 어르신 방문", location: "행복아파트 101동 202호", status: "completed" },
        { id: 2, time: "13:30", name: "이순자", title: "이순자 어르신 방문", location: "평화빌라 301호", status: "upcoming", tags: ["혈압 체크", "약 복용 확인"] },
        { id: 3, time: "15:00", name: "박동철", title: "박동철 어르신 방문", location: "무궁화주택 2층", status: "upcoming", tags: ["말벗", "식사 보조"] },
      ];
    } else if (d.diff < 0) { // 과거
      data[d.fullDate] = [
        { id: 4, time: "09:30", name: "최영희", title: "최영희 어르신 방문", location: "장미마을 202동", status: "completed", tags: ["병원 동행"] },
        { id: 5, time: "14:00", name: "정다은", title: "정다은 어르신 방문", location: "햇살요양원", status: "completed", tags: ["산책", "간식 보조"] },
      ];
    } else if (d.date % 2 === 0) {
      data[d.fullDate] = [
        { id: 6, time: "11:00", name: "박철수", title: "박철수 어르신 방문", location: "소망빌라 101호", status: "upcoming", tags: ["인지 훈련"] },
        { id: 7, time: "16:00", name: "이지은", title: "이지은 어르신 방문", location: "푸른언덕 3동", status: "upcoming", tags: ["투약 보조"] },
      ];
    } else {
      data[d.fullDate] = [
        { id: 8, time: "10:00", name: "강호동", title: "강호동 어르신 방문", location: "진달래아파트 502호", status: "upcoming", tags: ["식사 보조"] },
      ];
    }
  });
  return data;
};

interface HomePageProps {
  onNavigateToChecklist?: (visitId: number) => void;
}

export function HomePage({ onNavigateToChecklist }: HomePageProps) {
  const [dates] = useState(generateDates());
  const [mockData] = useState(() => generateMockDataForDates(dates));
  const [selectedDate, setSelectedDate] = useState(dates.find(d => d.isToday)?.fullDate || dates[0].fullDate);

  const selectedDateObj = dates.find(d => d.fullDate === selectedDate);
  const isTodaySelected = selectedDateObj?.isToday;

  const visits = mockData[selectedDate] || [];
  const nextVisit = visits.find(v => v.status === "upcoming");

  return (
    <div className="flex-1 overflow-y-auto pb-5 relative no-scrollbar flex flex-col bg-[#F7F5F0]">
      {/* Header */}
      <header className="flex justify-between items-center p-5 pt-6 sticky top-0 bg-[#F7F5F0]/90 backdrop-blur-sm z-10">
        <img src={logo} alt="HANA Logo" className="h-6 object-contain" />
      </header>

      {/* Greeting */}
      <div className="px-5 mb-3 mt-1 shrink-0">
        <h1 className="text-[20px] font-bold text-[#2B2E28]">안녕하세요 박복자님,</h1>
        <p className="text-[14px] text-[#6E756A] mt-0.5">오늘도 활기찬 하루 보내세요!</p>
      </div>

      {/* Content */}
      <div className="px-5 flex flex-col gap-6">

        {/* Date Selector */}
        <div className="flex justify-between items-start no-scrollbar">
          {dates.map((d) => {
            const isSelected = selectedDate === d.fullDate;
            return (
              <button
                key={d.fullDate}
                onClick={() => setSelectedDate(d.fullDate)}
                className="flex flex-col items-center gap-2 group"
              >
                <span className="text-[13px] font-bold text-[#8C8279]">{d.day}</span>
                <div className={`relative flex items-center justify-center w-11 h-11 rounded-full text-[17px] font-bold transition-all ${isSelected
                  ? "bg-[#89BAB1] text-white shadow-md shadow-[#89BAB1]/30"
                  : "bg-white text-[#2B2E28] hover:bg-[#F0ECE1]"
                  }`}
                >
                  {d.date}
                  {isSelected && (
                    <div className="absolute -bottom-2.5 w-1 h-1 rounded-full bg-[#89BAB1]"></div>
                  )}
                </div>
              </button>
            );
          })}
        </div>

        {/* Visit Schedule List */}
        <div>
          <div className="flex items-center justify-between mb-3 px-1">
            <h2 className="text-[17px] font-bold text-[#2B2E28] flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-[#E3EEE7] flex items-center justify-center text-[#89BAB1]">
                <Clock size={16} />
              </div>
              {isTodaySelected ? "오늘의 방문 일정" : `${selectedDateObj?.date}일의 방문 일정`}
            </h2>
          </div>
          <section className="bg-white rounded-xl p-5 shadow-sm border border-[#EBE8E0]">
            <div className="flex flex-col">
              {visits.map((visit, index) => {
                const isCompleted = visit.status === "completed";
                return (
                  <React.Fragment key={visit.id}>
                    <div className={`py-4 px-3 flex items-center gap-3 transition-all duration-300 ${isCompleted ? "bg-[#EAE7DF] rounded-[16px] opacity-70" : ""}`}>
                      <div className={`text-[16px] font-bold w-12 text-center shrink-0 ${isCompleted ? "text-[#8C8279]" : "text-[#89BAB1]"}`}>
                        {visit.time}
                      </div>

                      <div className="flex-1 min-w-0 px-2">
                        <h3 className={`font-bold text-[15px] truncate ${isCompleted ? "text-[#8C8279] line-through" : "text-[#2B2E28]"}`}>
                          {visit.title}
                        </h3>
                        <div className="text-[#8C8279] text-[12px] truncate mt-0.5">
                          {visit.location}
                        </div>
                      </div>

                      <button className={`text-[11px] font-bold px-2.5 py-1.5 rounded-full flex items-center gap-0.5 shrink-0 whitespace-nowrap ${isCompleted ? "text-[#8C8279] bg-white/50 border border-[#D1CCC5]" : "text-[#89BAB1] bg-[#E3EEE7]"}`}>
                        {isCompleted ? "방문 완료" : "방문 예정"} {isCompleted ? "" : <ChevronRight size={12} />}
                      </button>
                    </div>
                    {index < visits.length - 1 && !isCompleted && <div className="h-[1px] w-full bg-[#F0ECE1]"></div>}
                  </React.Fragment>
                );
              })}
            </div>

            <div className="mt-5 pt-5 border-t border-[#F0ECE1]">
              <div className="flex justify-between items-end mb-2">
                <span className="text-[13px] font-bold text-[#2B2E28]">체크리스트 달성률</span>
                <span className="text-[14px] font-bold text-[#89BAB1]">
                  {visits.filter(v => v.status === "completed").length} <span className="text-[#8C8279] text-[12px] font-normal">/ {visits.length}명</span>
                </span>
              </div>
              <div className="w-full h-1.5 bg-[#E3EEE7] rounded-full overflow-hidden">
                <div
                  className="h-full bg-[#89BAB1] rounded-full transition-all duration-500"
                  style={{ width: `${visits.length > 0 ? (visits.filter(v => v.status === "completed").length / visits.length) * 100 : 0}%` }}
                ></div>
              </div>
            </div>
          </section>
        </div>

        {/* Next Visit Summary */}
        {nextVisit && (
          <section className="mb-4">
            <h2 className="text-[19px] font-bold text-[#2B2E28] mb-4">다음 방문 요약</h2>

            <button
              onClick={() => onNavigateToChecklist?.(nextVisit.id)}
              className="w-full text-left bg-gradient-to-br from-[#89BAB1] to-[#1E3E35] rounded-[24px] p-6 text-white shadow-xl shadow-[#89BAB1]/20 relative overflow-hidden group hover:scale-[1.02] transition-transform duration-300"
            >
              <div className="absolute -right-4 -top-4 w-24 h-24 bg-white/10 rounded-full blur-2xl group-hover:scale-150 transition-transform duration-500"></div>

              <div className="flex justify-between items-start relative z-10">
                <div>
                  <div className="flex items-center gap-1.5 text-white/80 text-[14px] font-medium mb-1">
                    <Clock size={16} />
                    <span>{nextVisit.time} 예정</span>
                  </div>
                  <h3 className="text-[24px] font-bold mt-1 mb-2">
                    {nextVisit.name} 어르신
                  </h3>
                  <div className="flex items-center gap-1 text-white/80 text-[14px]">
                    <MapPin size={14} />
                    <span className="truncate">{nextVisit.location}</span>
                  </div>
                </div>

                <div className="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center backdrop-blur-md">
                  <ChevronRight size={20} className="text-white" />
                </div>
              </div>

              {nextVisit.tags && (
                <div className="flex gap-2 mt-5 relative z-10">
                  {nextVisit.tags.map((tag, idx) => (
                    <span key={idx} className="bg-white/20 backdrop-blur-md px-3 py-1.5 rounded-full text-[13px] font-medium text-white/95">
                      {tag}
                    </span>
                  ))}
                </div>
              )}
            </button>
          </section>
        )
        }

      </div >
    </div >
  );
}
