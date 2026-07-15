import React from "react";
import { Home, Mic, Users, Award } from "lucide-react";

type BottomNavProps = {
  tab: string;
  setTab: (t: string) => void;
};

export function BottomNav({ tab, setTab }: BottomNavProps) {
  const items = [
    { key: "home", label: "홈", icon: Home },
    { key: "voice", label: "일지 작성", icon: Mic },
    { key: "handover", label: "인수인계", icon: Users },
    { key: "portfolio", label: "포트폴리오", icon: Award },
  ];
  return (
    <nav className="flex border-t border-[#E7E2D3] bg-white pt-2 pb-2.5">
      {items.map((it) => (
        <button
          key={it.key}
          className={`flex-1 border-none bg-transparent cursor-pointer flex flex-col items-center gap-1 text-[12.5px] font-semibold py-1.5 ${
            tab === it.key ? "text-[#89BAB1]" : "text-[#6E756A]"
          }`}
          onClick={() => setTab(it.key)}
        >
          <it.icon size={22} />
          <span>{it.label}</span>
        </button>
      ))}
    </nav>
  );
}
