import React, { useState, useEffect } from "react";
import { ChevronLeft, Calendar, Activity, Utensils, Smile, Brain } from "lucide-react";
import { getVisitLogs } from "../../../shared/api";
import { VisitLogResponse } from "../../../shared/types";

type VisitLogHistoryProps = {
  elderId: string;
  elderName: string;
  onBack: () => void;
};

export function VisitLogHistory({ elderId, elderName, onBack }: VisitLogHistoryProps) {
  const [logs, setLogs] = useState<VisitLogResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await getVisitLogs(elderId);
        setLogs(res);
      } catch (e) {
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, [elderId]);

  return (
    <div className="flex-1 overflow-y-auto px-5 pt-[20px] pb-6 flex flex-col bg-[#F7F4EC]">
      <div className="flex items-center gap-2 mb-5 shrink-0">
        <button onClick={onBack} className="w-9 h-9 flex items-center justify-center rounded-full bg-white shadow-sm border border-[#E7E2D3] text-[#2B2E28]">
          <ChevronLeft size={20} />
        </button>
        <h1 className="font-['Gowun_Batang'] text-[21px] font-bold text-[#20423A] m-0 flex-1">{elderName}님의 방문 일지</h1>
      </div>

      {isLoading ? (
        <div className="flex flex-1 items-center justify-center">
          <div className="w-8 h-8 rounded-full border-[3px] border-[#E3EEE7] border-t-[#89BAB1] animate-spin" />
        </div>
      ) : logs.length === 0 ? (
        <div className="flex flex-1 items-center justify-center text-[#6E756A] text-[15px]">
          아직 작성된 방문 일지가 없습니다.
        </div>
      ) : (
        <div className="flex flex-col">
          {logs.map((log, i) => (
            <div key={log.logId} className="flex gap-3 relative">
              {i !== logs.length - 1 && (
                <div className="absolute top-[30px] bottom-[-4px] left-[5.5px] -translate-x-1/2 w-[2px] bg-[#8FB39A] opacity-50" />
              )}
              <div className="flex flex-col items-center w-[11px] shrink-0">
                <span className="w-[11px] h-[11px] rounded-full bg-[#8FB39A] mt-[19px] shrink-0 relative z-10" />
              </div>

              <div className="bg-white rounded-xl p-5 shadow-sm border border-[#E7E2D3] flex-1 mb-4 mt-1">
                <div className="flex items-center gap-1.5 text-[13px] text-[#A4A9A0] font-bold mb-2">
                  <Calendar size={13} />
                  {formatDate(log.createdAt)}
                </div>
                <p className="text-[15px] text-[#2B2E28] leading-relaxed mb-3">
                  {log.journalEntry || "종합 의견 없음"}
                </p>
                <div className="flex flex-col gap-2 border-t border-[#EBE8E0] pt-3">
                  <LogRow icon={Activity} label="신체" text={log.body} />
                  <LogRow icon={Utensils} label="영양" text={log.food} />
                  <LogRow icon={Smile} label="정서" text={log.emotion} />
                  <LogRow icon={Brain} label="인지" text={log.cognition} />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function LogRow({ icon: Icon, label, text }: { icon: any; label: string; text: string }) {
  if (!text) return null;
  return (
    <div className="flex items-start gap-2 text-[13.5px]">
      <Icon size={14} className="text-[#89BAB1] mt-0.5 shrink-0" />
      <span className="text-[#A4A9A0] font-bold shrink-0">{label}</span>
      <span className="text-[#2B2E28]">{text}</span>
    </div>
  );
}

function formatDate(iso: string): string {
  try {
    const d = new Date(iso);
    if (isNaN(d.getTime())) return iso;
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
  } catch {
    return iso;
  }
}
