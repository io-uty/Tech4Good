import React, { useState } from "react";
import { Toast } from "../../shared/ui/Toast";
import { BottomNav } from "../../widgets/bottom-nav/ui/BottomNav";
import { HomePage } from "../../pages/home/ui/HomePage";
import { VoiceLogPage } from "../../pages/voice-log/ui/VoiceLogPage";
import { HandoverPage } from "../../pages/handover/ui/HandoverPage";
import { PortfolioPage } from "../../pages/portfolio/ui/PortfolioPage";
import { ChecklistPage } from "../../pages/checklist/ui/ChecklistPage";

import "../styles/global.css";

export function App() {
  const [tab, setTab] = useState("home");
  const [toast, setToast] = useState<string | null>(null);
  const [selectedVisitId, setSelectedVisitId] = useState<number | null>(null);
  const [hideNav, setHideNav] = useState(false);

  function fire(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 2200);
  }

  const isPrimaryTab = ["home", "voice", "handover", "portfolio"].includes(tab);

  return (
    <div className="flex justify-center py-7 px-3 min-h-screen box-border font-['Noto_Sans_KR',sans-serif] text-[#2B2E28] bg-gradient-to-b from-[#EDE9DD] to-[#F7F4EC]">
      <div className="w-[390px] max-w-full bg-[#F7F4EC] rounded-[34px] shadow-[0_14px_34px_rgba(43,46,40,0.12)] border border-[#2B2E28]/[0.06] overflow-hidden flex flex-col h-[760px] relative">
        {tab === "home" && (
          <HomePage 
            onNavigateToChecklist={(id) => { 
              setSelectedVisitId(id); 
              setTab("checklist"); 
            }} 
          />
        )}
        {tab === "checklist" && (
          <ChecklistPage 
            visitId={selectedVisitId} 
            onBack={() => setTab("home")} 
          />
        )}
        {tab === "voice" && <VoiceLogPage onSubmit={() => fire("일지가 제출되었어요")} />}
        {tab === "handover" && <HandoverPage onShare={() => fire("다음 담당자에게 공유했어요")} setHideNav={setHideNav} />}
        {tab === "portfolio" && <PortfolioPage onDownload={() => fire("다운로드 준비 중이에요")} />}

        <Toast message={toast} />
        {isPrimaryTab && !hideNav && <BottomNav tab={tab} setTab={setTab} />}
      </div>
    </div>
  );
}

