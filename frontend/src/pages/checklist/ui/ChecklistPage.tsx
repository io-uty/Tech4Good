import React, { useState } from "react";
import { ChevronLeft, CheckCircle, Circle } from "lucide-react";

interface ChecklistPageProps {
  visitId: number | null;
  onBack: () => void;
}

const MOCK_CHECKLIST = [
  { id: 1, task: "혈압 및 혈당 측정하기", completed: false },
  { id: 2, task: "아침 약 복용 여부 확인", completed: true },
  { id: 3, task: "냉장고 유통기한 지난 음식 확인", completed: false },
  { id: 4, task: "실내 환기 및 청소 상태 점검", completed: false },
];

export function ChecklistPage({ visitId, onBack }: ChecklistPageProps) {
  const [tasks, setTasks] = useState(MOCK_CHECKLIST);

  const toggleTask = (id: number) => {
    setTasks(tasks.map(t => t.id === id ? { ...t, completed: !t.completed } : t));
  };

  const progress = Math.round((tasks.filter(t => t.completed).length / tasks.length) * 100);

  return (
    <div className="flex-1 flex flex-col bg-[#F7F4EC] h-full absolute inset-0 z-20">
      {/* Header */}
      <header className="flex items-center p-5 pt-6 sticky top-0 bg-[#F7F4EC]/90 backdrop-blur-sm z-10">
        <button 
          onClick={onBack}
          className="p-2 -ml-2 rounded-full hover:bg-black/5 transition-colors text-[#2B2E28]"
        >
          <ChevronLeft size={26} />
        </button>
        <h1 className="text-[18px] font-bold text-[#2B2E28] ml-2">방문 체크리스트</h1>
      </header>

      <div className="flex-1 overflow-y-auto px-5 pb-8 relative no-scrollbar">
        {/* Progress Card */}
        <div className="bg-white rounded-[24px] p-6 shadow-sm border border-[#E7E2D3] mb-6">
          <div className="flex justify-between items-end mb-4">
            <div>
              <p className="text-[#6E756A] text-[14px] font-medium mb-1">이순자 어르신</p>
              <h2 className="text-[22px] font-bold text-[#2B2E28]">진행률</h2>
            </div>
            <span className="text-[32px] font-extrabold text-[#89BAB1] leading-none">
              {progress}%
            </span>
          </div>
          
          <div className="w-full bg-[#F0ECE1] h-3 rounded-full overflow-hidden">
            <div 
              className="bg-[#89BAB1] h-full rounded-full transition-all duration-500 ease-out"
              style={{ width: `${progress}%` }}
            ></div>
          </div>
        </div>

        {/* Tasks List */}
        <div className="flex flex-col gap-3">
          {tasks.map(task => (
            <button
              key={task.id}
              onClick={() => toggleTask(task.id)}
              className={`flex items-start gap-4 p-5 rounded-[20px] transition-all duration-300 text-left ${
                task.completed 
                  ? "bg-[#89BAB1]/5 border border-[#89BAB1]/20" 
                  : "bg-white shadow-sm border border-[#E7E2D3]"
              }`}
            >
              <div className="mt-0.5">
                {task.completed ? (
                  <CheckCircle size={24} className="text-[#89BAB1]" />
                ) : (
                  <Circle size={24} className="text-[#C9C5B8]" />
                )}
              </div>
              <span className={`text-[16px] font-medium leading-snug flex-1 ${
                task.completed ? "text-[#6E756A] line-through" : "text-[#2B2E28]"
              }`}>
                {task.task}
              </span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
