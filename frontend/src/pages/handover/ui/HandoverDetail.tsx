import React, { useState, useEffect } from "react";
import { ChevronLeft, Info, HeartPulse, Home, Users, Gift, Share2, ChevronRight } from "lucide-react";
import { getElderProfile, getHandover } from "../../../shared/api";
import { ElderProfileType, HandoverResponse } from "../../../shared/types";
import { ThreadLine } from "../../../shared/ui/ThreadLine";

type HandoverDetailProps = {
  elderId: string;
  elderName: string;
  onBack: () => void;
  onShowServices: (services: any[]) => void;
  onShare: () => void;
};

export function HandoverDetail({ elderId, elderName, onBack, onShowServices, onShare }: HandoverDetailProps) {
  const [profile, setProfile] = useState<ElderProfileType | null>(null);
  const [handover, setHandover] = useState<HandoverResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [profileRes, handoverRes] = await Promise.all([
          getElderProfile(elderId),
          getHandover(elderId)
        ]);
        setProfile(profileRes);
        setHandover(handoverRes);
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
        <h1 className="font-['Gowun_Batang'] text-[21px] font-bold text-[#20423A] m-0 flex-1">{elderName}님의 인수인계서</h1>
      </div>

      {isLoading ? (
        <div className="flex flex-1 items-center justify-center">
          <div className="w-8 h-8 rounded-full border-[3px] border-[#E3EEE7] border-t-[#89BAB1] animate-spin" />
        </div>
      ) : profile && handover ? (
        <div className="flex flex-col gap-6">
          
          {/* 기본 인적 사항 */}
          <section>
            <h2 className="text-[17px] font-bold text-[#2B2E28] mb-3 flex items-center gap-1.5">
              <span className="w-[5px] h-[5px] bg-[#89BAB1] rounded-full"></span> 기본 인적 사항
            </h2>
            <div className="bg-white rounded-xl p-5 shadow-sm border border-[#E7E2D3] flex flex-col gap-4">
              <InfoRow icon={Info} label="생년월일" value={profile.birthDate} />
              <InfoRow icon={Home} label="주소" value={profile.address} />
              <InfoRow icon={Users} label="가족관계" value={profile.family} />
              <InfoRow icon={HeartPulse} label="건강상태" value={profile.health} />
              <InfoRow icon={Gift} label="지원 물품" value={profile.governmentSupport.join(", ")} />
              
              <button 
                onClick={() => onShowServices(profile.linkedServices)}
                className="mt-2 bg-[#F0ECE1] rounded-xl p-4 flex items-center justify-between hover:bg-[#E3EEE7] transition-colors cursor-pointer text-left w-full border border-[#DDD8C8]"
              >
                <div>
                  <div className="text-[14px] font-bold text-[#89BAB1] mb-0.5">연계 서비스 현황</div>
                  <div className="text-[13px] text-[#6E756A]">{profile.linkedServices.length}개의 서비스를 받고 계십니다</div>
                </div>
                <div className="w-8 h-8 rounded-full bg-white flex items-center justify-center shadow-sm">
                  <ChevronRight size={18} className="text-[#89BAB1]" />
                </div>
              </button>
            </div>
          </section>

          {/* 대화 팁 */}
          <section>
            <h2 className="text-[17px] font-bold text-[#2B2E28] mb-3 flex items-center gap-1.5">
              <span className="w-[5px] h-[5px] bg-[#89BAB1] rounded-full"></span> 대화 팁
            </h2>
            <div className="flex flex-col">
              {handover.tips.map((t, i) => (
                <div key={i} className="flex gap-3 relative group">
                  {/* 타임라인 선 (절대 위치로 현재 아이템의 끝부분까지 이어짐) */}
                  {i !== handover.tips.length - 1 && (
                    <div className="absolute top-[30px] bottom-[-4px] left-[5.5px] -translate-x-1/2 w-[2px] bg-[#8FB39A] opacity-50" />
                  )}
                  
                  {/* 타임라인 점 */}
                  <div className="flex flex-col items-center w-[11px] shrink-0">
                    <span className="w-[11px] h-[11px] rounded-full bg-[#8FB39A] mt-[19px] shrink-0 relative z-10" />
                  </div>
                  
                  {/* 말풍선 카드 */}
                  <div className={`rounded-xl py-[13px] px-[15px] text-[15.5px] leading-relaxed mb-3 mt-1 flex-1 shadow-sm ${
                    t.caution ? "bg-[#F5E3DD] text-[#B5533C] border border-[#B5533C]/25 font-medium" : "bg-white text-[#2B2E28] border border-[#EBE8E0]"
                  }`}>
                    {t.text}
                  </div>
                </div>
              ))}
            </div>
          </section>

          <button className="bg-[#89BAB1] text-white border-none rounded-xl p-4 text-[17px] font-bold cursor-pointer w-full mt-2 shrink-0 flex items-center justify-center gap-2 mb-2" onClick={onShare}>
            <Share2 size={18} />
            다음 담당자에게 공유
          </button>
        </div>
      ) : (
        <div className="flex flex-1 items-center justify-center text-[#6E756A] text-[15px]">
          데이터를 불러오지 못했습니다.
        </div>
      )}
    </div>
  );
}

function InfoRow({ icon: Icon, label, value }: { icon: any, label: string, value: string }) {
  return (
    <div className="flex items-start gap-3">
      <div className="w-7 h-7 rounded-full bg-[#F6E9D2] flex items-center justify-center shrink-0 mt-0.5">
        <Icon size={14} className="text-[#C98A2B]" />
      </div>
      <div>
        <div className="text-[12.5px] text-[#A4A9A0] font-bold mb-0.5">{label}</div>
        <div className="text-[14.5px] text-[#2B2E28] leading-snug">{value}</div>
      </div>
    </div>
  );
}
