import React, { useState, useEffect } from "react";
import { getPortfolio } from "../../../shared/api";
import { PortfolioResponse } from "../../../shared/types";
import { PortfolioMain } from "./PortfolioMain";

export function PortfolioPage({ onDownload }: { onDownload: () => void }) {
  const [data, setData] = useState<PortfolioResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const res = await getPortfolio("worker-001");
        setData(res);
      } catch (e) {
        console.error(e);
      } finally {
        setIsLoading(false);
      }
    }
    load();
  }, []);

  if (isLoading) {
    return (
      <div className="flex flex-1 items-center justify-center bg-[#F7F5F0]">
        <div className="w-8 h-8 rounded-full border-[3px] border-[#E3EEE7] border-t-[#89BAB1] animate-spin" />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="flex flex-1 items-center justify-center text-[#4A5046] text-[15px] bg-[#F7F5F0]">
        데이터가 없습니다.
      </div>
    );
  }

  return <PortfolioMain data={data} onDownload={onDownload} />;
}
