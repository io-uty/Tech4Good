import React, { useState } from "react";
import { Activity, Utensils, Smile, Brain, Send, Mic, Square, Clock, Edit2, Bell } from "lucide-react";
import { ThreadSquiggle } from "../../../shared/ui/ThreadSquiggle";
import { submitVisitLog, submitStt } from "../../../shared/api";
import { VisitLogResponse, ResultCardType } from "../../../shared/types";
import ezBefore from "../../../beforeez.webp";
import ezSpeaking from "../../../speakingez.webp";
import ezDone from "../../../doneez.webp";

type RecordState = "idle" | "recording" | "transcribing" | "textInput" | "summarizing" | "finalSummary";

export function VoiceLogPage({ onSubmit }: { onSubmit: () => void }) {
  const [recordState, setRecordState] = useState<RecordState>("idle");
  const [transcribedText, setTranscribedText] = useState("");
  const [text, setText] = useState("");
  const [result, setResult] = useState<VisitLogResponse | null>(null);

  const handleStartRecording = () => {
    setRecordState("recording");
  };

  const handleStopRecording = async () => {
    setRecordState("transcribing");

    // 녹음 후 로딩 페이지 1초 보이게 함
    await new Promise(resolve => setTimeout(resolve, 1000));

    try {
      const dummyAudio = new Blob(["dummy audio content"], { type: "audio/wav" });
      const res = await submitStt(dummyAudio);
      setTranscribedText(res.rawText);
      setRecordState("textInput");
    } catch (e) {
      console.error(e);
      setTranscribedText("어르신께서 오늘 아침 식사로 죽을 반 그릇 드셨고, 기분은 대체로 평온해 보이셨습니다. 낮에는 거실에서 TV를 보며 시간을 보내셨고 특별한 건강 이상은 없으셨습니다.");
      setRecordState("textInput");
    }
  };

  const handleSubmit = async () => {
    setRecordState("summarizing");
    try {
      const combinedText = transcribedText + (text ? "\n\n추가 내용: " + text : "");
      const res = await submitVisitLog("worker-1", "elder-1", combinedText);
      setResult(res);
      setRecordState("finalSummary");
    } catch (e) {
      console.error(e);
      alert("일지 제출에 실패했습니다. (API 서버가 없습니다)");
      setRecordState("textInput");
    }
  };

  const handleReset = () => {
    setTranscribedText("");
    setText("");
    setResult(null);
    setRecordState("idle");
  };

  const resultCards: ResultCardType[] = result
    ? [
      { key: "journalEntry", label: "종합 의견", icon: Edit2, text: result.journalEntry || "의견 없음" },
      { key: "body", label: "신체 상태", icon: Activity, text: result.body },
      { key: "food", label: "영양(식사)", icon: Utensils, text: result.food },
      { key: "emotion", label: "정서 상태", icon: Smile, text: result.emotion },
      { key: "cognition", label: "인지 상태", icon: Brain, text: result.cognition },
    ]
    : [];

  const LoadingSkeleton = ({ message, timeMsg }: { message: string, timeMsg?: string }) => (
    <div className="flex-1 bg-white rounded-xl p-6 shadow-sm border border-[#E7E2D3] flex flex-col mt-2">
      <div className="flex flex-col items-center justify-center mb-8 mt-4">
        <div className="w-10 h-10 border-4 border-[#E3EEE7] border-t-[#89BAB1] rounded-full animate-spin mb-4"></div>
        <h3 className="text-[17px] font-bold text-[#2B2E28]">{message}</h3>
        {timeMsg && (
          <div className="flex items-center gap-1.5 text-[#4A5046] font-medium mt-2 bg-[#F0ECE1]/50 px-3 py-1 rounded-full text-[13px]">
            <Clock size={14} /> {timeMsg}
          </div>
        )}
      </div>
      <div className="space-y-4">
        <div className="h-5 bg-[#F0ECE1] rounded-full animate-pulse w-full"></div>
        <div className="h-5 bg-[#F0ECE1] rounded-full animate-pulse w-[92%]"></div>
        <div className="h-5 bg-[#F0ECE1] rounded-full animate-pulse w-[88%]"></div>
        <div className="h-5 bg-[#F0ECE1] rounded-full animate-pulse w-[75%]"></div>
        <div className="h-5 bg-[#F0ECE1] rounded-full animate-pulse w-[85%] mt-8"></div>
        <div className="h-5 bg-[#F0ECE1] rounded-full animate-pulse w-[90%]"></div>
      </div>
    </div>
  );

  return (
    <div className="flex-1 overflow-y-auto pb-5 relative flex flex-col w-full h-full bg-[#F7F4EC]">
      {/* Header */}
      <header className="flex justify-between items-center p-5 pt-6 sticky top-0 bg-[#F7F4EC]/90 backdrop-blur-sm z-10 shrink-0">

        <h1 className="text-[25px] font-bold mt-2 mb-1 text-[#2B2E28]">방문 일지</h1>
      </header>

      <div className="px-5 mb-6 shrink-0 w-full">
        <p className="text-[15px] text-[#4A5046] m-0">작성하신 내용을 AI가 정리해 드려요</p>
      </div>

      <div className="flex flex-col flex-1 relative h-full w-full">

        {/* Idle / Recording / Transcribing State */}
        {(recordState === "idle" || recordState === "recording" || recordState === "transcribing") && (
          <div className="absolute inset-0 flex flex-col items-center justify-center w-full h-full pb-10">

            {/* Character Area */}
            <div className="flex flex-col items-center -mt-24">
              <div className="w-[270px] h-[270px] relative flex items-center justify-center mb-6">
                {recordState === "recording" && (
                  <div className="absolute inset-0 border-[6px] border-[#E3EEE7] border-t-[#89BAB1] rounded-full animate-spin"></div>
                )}
                <img
                  src={recordState === "idle" ? ezBefore : recordState === "recording" ? ezSpeaking : ezDone}
                  alt="캐릭터"
                  className={`w-full h-full object-contain relative z-10 ${recordState === "recording" ? "animate-gentle-bounce" : ""}`}
                />
              </div>

              {/* Text hidden in idle state */}
              {recordState !== "idle" && (
                <p className="text-[#89BAB1] font-bold text-[18px]">
                  {recordState === "recording" ? "이야기를 듣고 있어요..." : "성공적으로 녹음되었어요!"}
                </p>
              )}

              <div className="h-[20px] flex items-center justify-center mt-4">
                {recordState === "recording" && (
                  <div className="flex gap-1.5">
                    <div className="w-2 h-2 bg-[#89BAB1] rounded-full animate-bounce" style={{ animationDelay: "0ms" }}></div>
                    <div className="w-2 h-2 bg-[#89BAB1] rounded-full animate-bounce" style={{ animationDelay: "150ms" }}></div>
                    <div className="w-2 h-2 bg-[#89BAB1] rounded-full animate-bounce" style={{ animationDelay: "300ms" }}></div>
                  </div>
                )}
              </div>
            </div>

            {/* Record / Stop Button */}
            {recordState !== "transcribing" && (
              <div className="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center w-max">
                <button
                  onClick={recordState === "idle" ? handleStartRecording : handleStopRecording}
                  className={`flex items-center justify-center rounded-full shadow-lg transition-all duration-500 ${recordState === "idle"
                    ? "w-[128px] h-[128px] bg-gradient-to-br from-[#89BAB1] to-[#B0CDB6] text-white hover:scale-105"
                    : "w-[100px] h-[100px] bg-red-500 text-white hover:scale-105"
                    }`}
                >
                  {recordState === "idle" ? <Mic size={64} /> : <Square size={40} fill="currentColor" />}
                </button>
                {recordState === "idle" && (
                  <p className="text-[#4A5046] mt-6 font-medium text-[16px]">마이크를 눌러 기록을 시작하세요</p>
                )}
              </div>
            )}
          </div>
        )}

        {/* Text Input State */}
        {recordState === "textInput" && (
          <div className="flex flex-col flex-1 gap-5 animate-in fade-in slide-in-from-bottom-4 duration-500 relative z-10 h-full">
            {transcribedText && (
              <div className="bg-white rounded-xl p-5 border border-[#E7E2D3] shadow-sm shrink-0">
                <div className="flex items-center gap-2 mb-3">
                  <Mic size={18} className="text-[#89BAB1]" />
                  <h3 className="text-[15px] font-bold text-[#2B2E28]">음성 기록 내용</h3>
                </div>
                <p className="text-[15.5px] text-[#2B2E28] leading-relaxed tracking-tight">{transcribedText}</p>
              </div>
            )}

            <div className="flex flex-col flex-1">
              <h3 className="text-[14px] font-bold text-[#4A5046] mb-2 flex items-center gap-1.5 ml-1">
                <Edit2 size={16} /> 직접 타이핑하기
              </h3>
              <textarea
                className="flex-1 resize-none w-full bg-white rounded-xl p-4 shadow-sm border border-[#E7E2D3] outline-none text-[#2B2E28] placeholder:text-[#A4A9A0] text-[15px] leading-relaxed"
                placeholder="추가로 남기고 싶은 내용이 있다면 적어주세요..."
                value={text}
                onChange={(e) => setText(e.target.value)}
              />
            </div>
            <button
              className="shrink-0 bg-[#89BAB1] text-white border-none rounded-xl p-4 text-[17px] font-bold cursor-pointer w-full flex items-center justify-center gap-2 transition-transform active:scale-95"
              onClick={handleSubmit}
            >
              <Send size={18} />
              요약 정리하기
            </button>
          </div>
        )}

        {/* Summarizing Skeleton State */}
        {recordState === "summarizing" && (
          <LoadingSkeleton message="AI가 내용을 요약 정리 중입니다" timeMsg="예상 소요 시간 3초" />
        )}

        {/* Final Summary State */}
        {recordState === "finalSummary" && result && (
          <div className="flex flex-col flex-1 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <h2 className="text-[18px] font-bold text-[#2B2E28] mb-4 flex items-center gap-2">
              <span className="w-1.5 h-5 bg-[#89BAB1] rounded-full"></span>최종 정리본
            </h2>
            <div className="flex flex-col gap-3 mb-6 flex-1 overflow-y-auto no-scrollbar pb-4">
              {resultCards.map((c) => (
                <div className="flex gap-3 items-start bg-white rounded-xl p-[16px] shadow-sm border border-[#E7E2D3]" key={c.key}>
                  <div className="w-[42px] h-[42px] rounded-xl bg-[#F0ECE1] text-[#89BAB1] flex items-center justify-center shrink-0">
                    <c.icon size={22} />
                  </div>
                  <div className="flex-1 mt-0.5">
                    <div className="text-[13.5px] text-[#4A5046] mb-1 font-bold">{c.label}</div>
                    <div className="text-[16px] leading-relaxed text-[#2B2E28] font-medium tracking-tight">{c.text}</div>
                  </div>
                </div>
              ))}
            </div>

            <div className="flex gap-3 mt-auto pt-2 pb-6 mb-2 shrink-0 px-5">
              <button
                className="bg-white text-[#4A5046] border border-[#E7E2D3] rounded-2xl p-4 text-[16px] font-bold cursor-pointer flex-1 flex items-center justify-center shadow-sm hover:bg-[#F9F8F5] transition-colors"
                onClick={handleReset}
              >
                다시 작성
              </button>
              <button
                className="bg-[#89BAB1] text-white border-none rounded-2xl p-4 text-[17px] font-bold cursor-pointer flex-1 flex items-center justify-center gap-2 shadow-lg shadow-[#89BAB1]/20 hover:opacity-90 transition-colors"
                onClick={() => {
                  onSubmit();
                  handleReset();
                }}
              >
                완료
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
