import React, { useState, useEffect } from "react";
import { ChevronLeft, Info, HeartPulse, Home, Users, Gift, Share2, ChevronRight, MessageCircle, AlertTriangle, Heart, Ban, FileClock } from "lucide-react";
import { getElderProfile, getHandover } from "../../../shared/api";
import { ElderProfileType, HandoverResponse } from "../../../shared/types";
import { ThreadLine } from "../../../shared/ui/ThreadLine";

type HandoverDetailProps = {
  elderId: string;
  elderName: string;
  onBack: () => void;
  onShowServices: (services: any[]) => void;
  onShowLogs: () => void;
  onShare: () => void;
};

export function HandoverDetail({ elderId, elderName, onBack, onShowServices, onShowLogs, onShare }: HandoverDetailProps) {
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
    <div className="flex-1 overflow-y-auto px-5 flex flex-col bg-[#F7F4EC] relative">
      <div className="flex items-center gap-2 mb-5 shrink-0 sticky top-0 bg-[#F7F4EC]/90 backdrop-blur-sm z-20 pt-6 pb-4 -mx-5 px-5">
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
                  <div className="text-[13px] text-[#4A5046]">{profile.linkedServices.length}개의 서비스를 받고 계십니다</div>
                </div>
                <div className="w-8 h-8 rounded-full bg-white flex items-center justify-center shadow-sm">
                  <ChevronRight size={18} className="text-[#89BAB1]" />
                </div>
              </button>

              <button
                onClick={onShowLogs}
                className="mt-2 bg-[#F0ECE1] rounded-xl p-4 flex items-center justify-between hover:bg-[#E3EEE7] transition-colors cursor-pointer text-left w-full border border-[#DDD8C8]"
              >
                <div>
                  <div className="text-[14px] font-bold text-[#89BAB1] mb-0.5">일지 확인</div>
                  <div className="text-[13px] text-[#6E756A]">그동안의 방문 기록을 모두 확인해요</div>
                </div>
                <div className="w-8 h-8 rounded-full bg-white flex items-center justify-center shadow-sm">
                  <ChevronRight size={18} className="text-[#89BAB1]" />
                </div>
              </button>
            </div>
          </section>

          {/* 성향 및 대화 특징 */}
          <section>
            <h2 className="text-[17px] font-bold text-[#2B2E28] mb-3 flex items-center gap-1.5">
              <span className="w-[5px] h-[5px] bg-[#89BAB1] rounded-full"></span> 성향 및 대화 특징
            </h2>
            <div className="bg-white rounded-xl p-5 shadow-sm border border-[#E7E2D3] flex items-start gap-3">
              <MessageCircle size={16} className="text-[#89BAB1] mt-0.5 shrink-0" />
              <p className="text-[15px] text-[#2B2E28] leading-relaxed m-0">
                {handover.summary.personality || "아직 파악된 성향 정보가 없습니다."}
              </p>
            </div>
          </section>

          {/* 정서 트리거 */}
          {handover.summary.emotionalTriggers.length > 0 && (
            <section>
              <h2 className="text-[17px] font-bold text-[#2B2E28] mb-3 flex items-center gap-1.5">
                <span className="w-[5px] h-[5px] bg-[#89BAB1] rounded-full"></span> 정서 트리거
              </h2>
              <div className="flex flex-col gap-3">
                {handover.summary.emotionalTriggers.map((trig, i) => (
                  <div key={i} className="bg-[#F5E3DD] border border-[#B5533C]/25 rounded-xl p-4">
                    <div className="flex items-start gap-2 mb-1.5">
                      <AlertTriangle size={15} className="text-[#B5533C] mt-0.5 shrink-0" />
                      <div className="text-[14.5px] font-bold text-[#B5533C] leading-snug">{trig.trigger}</div>
                    </div>
                    <p className="text-[13.5px] text-[#2B2E28] leading-relaxed mb-2 ml-[23px]">{trig.description}</p>
                    <div className="flex flex-wrap gap-1.5 ml-[23px]">
                      {trig.sourceLogIds.map((logId) => (
                        <span key={logId} className="text-[11px] text-[#89BAB1] bg-white rounded-full px-2 py-0.5 border border-[#DDD8C8]">
                          근거: {logId}
                        </span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </section>
          )}

          {/* 선호·금기 화제 */}
          {(handover.summary.preferredTopics.length > 0 || handover.summary.avoidTopics.length > 0) && (
            <section>
              <h2 className="text-[17px] font-bold text-[#2B2E28] mb-3 flex items-center gap-1.5">
                <span className="w-[5px] h-[5px] bg-[#89BAB1] rounded-full"></span> 선호·금기 화제
              </h2>
              <div className="bg-white rounded-xl p-5 shadow-sm border border-[#E7E2D3] flex flex-col gap-4">
                {handover.summary.preferredTopics.length > 0 && (
                  <div className="flex items-start gap-3">
                    <Heart size={14} className="text-[#89BAB1] mt-1 shrink-0" />
                    <div className="flex flex-wrap gap-2">
                      {handover.summary.preferredTopics.map((topic, i) => (
                        <span key={i} className="text-[13px] text-[#3E7A6B] bg-[#E3EEE7] rounded-full px-3 py-1 border border-[#89BAB1]/25">
                          {topic}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
                {handover.summary.avoidTopics.length > 0 && (
                  <div className="flex items-start gap-3">
                    <Ban size={14} className="text-[#B5533C] mt-1 shrink-0" />
                    <div className="flex flex-wrap gap-2">
                      {handover.summary.avoidTopics.map((topic, i) => (
                        <span key={i} className="text-[13px] text-[#B5533C] bg-[#F5E3DD] rounded-full px-3 py-1 border border-[#B5533C]/25">
                          {topic}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </section>
          )}

          {/* 최근 3개월 요약 */}
          {handover.summary.recentThreeMonthSummary && (
            <section>
              <h2 className="text-[17px] font-bold text-[#2B2E28] mb-3 flex items-center gap-1.5">
                <span className="w-[5px] h-[5px] bg-[#89BAB1] rounded-full"></span> 최근 3개월 요약
              </h2>
              <div className="bg-white rounded-xl p-5 shadow-sm border border-[#E7E2D3] flex items-start gap-3">
                <FileClock size={16} className="text-[#89BAB1] mt-0.5 shrink-0" />
                <p className="text-[15px] text-[#2B2E28] leading-relaxed m-0">{handover.summary.recentThreeMonthSummary}</p>
              </div>
            </section>
          )}
        </div>
      ) : (
        <div className="flex flex-1 items-center justify-center text-[#4A5046] text-[15px]">
          데이터를 불러오지 못했습니다.
        </div>
      )}

      {profile && handover && (
        <div className="sticky bottom-0 left-0 right-0 z-20 bg-[#F7F4EC]/90 backdrop-blur-sm pt-4 pb-6 mt-auto -mx-5 px-5">
          <button className="bg-[#89BAB1] text-white border-none rounded-xl p-4 text-[17px] font-bold cursor-pointer w-full flex items-center justify-center gap-2 shadow-lg shadow-[#89BAB1]/20 hover:opacity-90 transition-colors" onClick={onShare}>
            <Share2 size={18} />
            다음 담당자에게 공유
          </button>
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
