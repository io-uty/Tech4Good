import React, { useState, useEffect } from "react";
import { HandoverList } from "./HandoverList";
import { HandoverDetail } from "./HandoverDetail";
import { LinkedServices } from "./LinkedServices";
import { VisitLogHistory } from "./VisitLogHistory";
import { LinkedServiceType } from "../../../shared/types";

export function HandoverPage({ onShare, setHideNav }: { onShare: () => void, setHideNav?: (hide: boolean) => void }) {
  const [view, setView] = useState<"list" | "detail" | "services" | "logs">("list");
  const [selectedElder, setSelectedElder] = useState<{ id: string; name: string } | null>(null);
  const [servicesData, setServicesData] = useState<LinkedServiceType[]>([]);

  useEffect(() => {
    if (setHideNav) {
      setHideNav(view !== "list");
    }
    // Cleanup to ensure nav shows when unmounting HandoverPage altogether
    return () => {
      if (setHideNav) setHideNav(false);
    };
  }, [view, setHideNav]);

  const handleSelectElder = (id: string, name: string) => {
    setSelectedElder({ id, name });
    setView("detail");
  };

  const handleShowServices = (services: LinkedServiceType[]) => {
    setServicesData(services);
    setView("services");
  };

  const handleShowLogs = () => {
    setView("logs");
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
        onShowLogs={handleShowLogs}
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

  if (view === "logs" && selectedElder) {
    return (
      <VisitLogHistory
        elderId={selectedElder.id}
        elderName={selectedElder.name}
        onBack={() => setView("detail")}
      />
    );
  }

  return null;
}
