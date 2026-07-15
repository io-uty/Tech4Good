import React, { useState } from "react";
import { HandoverList } from "./HandoverList";
import { HandoverDetail } from "./HandoverDetail";
import { LinkedServices } from "./LinkedServices";
import { LinkedServiceType } from "../../../shared/types";

export function HandoverPage({ onShare }: { onShare: () => void }) {
  const [view, setView] = useState<"list" | "detail" | "services">("list");
  const [selectedElder, setSelectedElder] = useState<{ id: string; name: string } | null>(null);
  const [servicesData, setServicesData] = useState<LinkedServiceType[]>([]);

  const handleSelectElder = (id: string, name: string) => {
    setSelectedElder({ id, name });
    setView("detail");
  };

  const handleShowServices = (services: LinkedServiceType[]) => {
    setServicesData(services);
    setView("services");
  };

  if (view === "list") {
    return <HandoverList onSelect={handleSelectElder} />;
  }

  if (view === "detail" && selectedElder) {
    return (
      <HandoverDetail
        elderId={selectedElder.id}
        elderName={selectedElder.name}
        onBack={() => setView("list")}
        onShowServices={handleShowServices}
        onShare={onShare}
      />
    );
  }

  if (view === "services" && selectedElder) {
    return (
      <LinkedServices
        elderName={selectedElder.name}
        services={servicesData}
        onBack={() => setView("detail")}
      />
    );
  }

  return null;
}
