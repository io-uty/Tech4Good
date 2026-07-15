import React from "react";
import { Check } from "lucide-react";

export function Toast({ message }: { message: string | null }) {
  if (!message) return null;
  return (
    <div className="absolute bottom-[90px] left-1/2 -translate-x-1/2 bg-[#20423A] text-white py-3 px-5 rounded-full text-[14.5px] font-semibold flex items-center gap-2 shadow-[0_8px_20px_rgba(0,0,0,0.25)] z-20 whitespace-nowrap">
      <Check size={18} />
      <span>{message}</span>
    </div>
  );
}
