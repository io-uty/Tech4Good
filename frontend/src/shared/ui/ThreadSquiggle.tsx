import React from "react";

export function ThreadSquiggle({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 320 40" preserveAspectRatio="none" aria-hidden="true">
      <path
        d="M0 24 C 40 4, 80 44, 120 24 S 200 4, 240 24 S 300 40, 320 20"
        fill="none"
        stroke="#8FB39A"
        strokeWidth="2.5"
        strokeLinecap="round"
        opacity="0.55"
      />
    </svg>
  );
}
