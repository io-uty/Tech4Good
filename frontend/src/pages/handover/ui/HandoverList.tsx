import React, { useState, useEffect } from "react";
import { ChevronRight, User, Bell } from "lucide-react";
import { ThreadSquiggle } from "../../../shared/ui/ThreadSquiggle";
import { getAssignedElders } from "../../../shared/api";
import { AssignedElderType } from "../../../shared/types";

export function HandoverList({ onSelect }: { onSelect: (id: string, name: string) => void }) {
  const [elders, setElders] = useState<AssignedElderType[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await getAssignedElders("worker-1");
        setElders(res);
      } catch (e) {
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  return (
    <div className="flex-1 overflow-y-auto pb-6 flex flex-col bg-[#F7F4EC]">
      {/* Header */}
      <header className="flex justify-between items-center p-5 pt-6 sticky top-0 bg-[#F7F4EC]/90 backdrop-blur-sm z-10 shrink-0">
        <div className="text-[22px] font-extrabold text-[#89BAB1] tracking-tight">HANA</div>
        <button className="relative p-2 rounded-full hover:bg-black/5 transition-colors text-[#2B2E28]">
          <Bell size={24} strokeWidth={2.5} />
        </button>
      </header>

      <div className="px-5 mb-6 shrink-0 w-full">
        <h1 className="text-[25px] font-bold mt-2 mb-1 text-[#2B2E28]">인수인계 브리핑</h1>
        <p className="text-[15px] text-[#6E756A] m-0">담당 중인 어르신을 선택해 주세요</p>
      </div>

      {isLoading ? (
        <div className="flex flex-1 items-center justify-center">
          <div className="w-8 h-8 rounded-full border-[3px] border-[#E3EEE7] border-t-[#89BAB1] animate-spin" />
        </div>
      ) : (
        <div className="flex flex-col gap-4">
          {elders.map((elder) => (
            <button
              key={elder.elderId}
              onClick={() => onSelect(elder.elderId, elder.name)}
              className="bg-white rounded-xl p-[18px] shadow-[0_4px_14px_rgba(43,46,40,0.06)] border border-[#E7E2D3] flex items-center justify-between hover:bg-[#F0ECE1] transition-colors cursor-pointer text-left w-full"
            >
              <div className="flex items-center gap-[14px]">
                <div className="w-[52px] h-[52px] rounded-full bg-[#F6E9D2] text-[#C98A2B] font-['Gowun_Batang'] text-[22px] font-bold flex items-center justify-center shrink-0">
                  {elder.name[0]}
                </div>
                <div>
                  <div className="text-[17px] font-bold text-[#2B2E28] mb-1">
                    {elder.name} 어르신 <span className="text-[14px] text-[#6E756A] font-medium ml-1">({elder.gender}, {elder.age}세)</span>
                  </div>
                  <div className="text-[13px] text-[#6E756A]">{elder.address}</div>
                </div>
              </div>
              <ChevronRight className="text-[#A4A9A0]" size={20} />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
