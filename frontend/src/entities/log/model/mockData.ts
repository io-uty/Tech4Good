import { Activity, Utensils, Smile, Brain } from "lucide-react";
import { ResultCardType } from "../../../shared/types";

export const RESULT_CARDS: ResultCardType[] = [
  { key: "body", label: "신체 상태", icon: Activity, text: "무릎 통증 호소 없음, 걸음걸이 평소와 비슷함" },
  { key: "food", label: "영양(식사)", icon: Utensils, text: "점심 절반 정도 드심, 입맛 없다고 하셔서 식사 독려함" },
  { key: "emotion", label: "정서 상태", icon: Smile, text: "아드님 이야기 나오자 말수 줄어듦, 평소보다 조용하심" },
  { key: "cognition", label: "인지 상태", icon: Brain, text: "날짜·요일 정확히 인지, 대화 흐름 자연스러움" },
];
