import React from "react";
import { ChevronLeft, Info, Calendar, Building2 } from "lucide-react";
import { LinkedServiceType } from "../../../shared/types";

type LinkedServicesProps = {
  elderName: string;
  services: LinkedServiceType[];
  onBack: () => void;
};

export function LinkedServices({ elderName, services, onBack }: LinkedServicesProps) {
  return (
    <div className="flex-1 overflow-y-auto px-5 pt-[20px] pb-6 flex flex-col bg-[#F7F4EC]">
      <div className="flex items-center gap-2 mb-5 shrink-0">
        <button onClick={onBack} className="w-9 h-9 flex items-center justify-center rounded-full bg-white shadow-sm border border-[#E7E2D3] text-[#2B2E28]">
          <ChevronLeft size={20} />
        </button>
        <h1 className="font-['Gowun_Batang'] text-[21px] font-bold text-[#20423A] m-0 flex-1">{elderName}님 연계 서비스</h1>
      </div>

      <div className="flex flex-col gap-4">
        {services.map((service, i) => (
          <div key={i} className="bg-white rounded-xl p-5 shadow-[0_4px_14px_rgba(43,46,40,0.06)] border border-[#E7E2D3]">
            <h3 className="text-[17px] font-bold text-[#89BAB1] mb-3">{service.serviceName}</h3>
            
            <div className="flex flex-col gap-2.5">
              <div className="flex items-start gap-2.5">
                <Building2 size={15} className="text-[#A4A9A0] mt-0.5" />
                <div>
                  <div className="text-[12px] text-[#A4A9A0] font-bold">제공 기관</div>
                  <div className="text-[14px] text-[#2B2E28]">{service.provider}</div>
                </div>
              </div>
              
              <div className="flex items-start gap-2.5">
                <Calendar size={15} className="text-[#A4A9A0] mt-0.5" />
                <div>
                  <div className="text-[12px] text-[#A4A9A0] font-bold">이용 기간</div>
                  <div className="text-[14px] text-[#2B2E28]">{service.period}</div>
                </div>
              </div>

              <div className="flex items-start gap-2.5 bg-[#F7F4EC] p-3 rounded-xl mt-1">
                <Info size={15} className="text-[#C98A2B] mt-0.5 shrink-0" />
                <div className="text-[13.5px] text-[#2B2E28] leading-relaxed">
                  {service.description}
                </div>
              </div>
            </div>
          </div>
        ))}

        {services.length === 0 && (
          <div className="bg-white rounded-xl p-8 text-center text-[#6E756A] shadow-sm border border-[#E7E2D3]">
            현재 받고 계신 연계 서비스가 없습니다.
          </div>
        )}
      </div>
    </div>
  );
}
