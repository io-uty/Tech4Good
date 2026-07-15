import { HandoverType } from "../../../shared/types";

export const HANDOVER: HandoverType = {
  name: "김순이",
  years: "3년째 돌봄",
  tips: [
    { text: "귀가 어두우시니 낮은 톤으로 천천히 말씀해 주세요.", caution: false },
    { text: "트로트 가수 임영웅의 노래를 틀어드리면 마음을 쉽게 여십니다.", caution: false },
    { text: "5월에는 사별한 배우자 생각으로 우울증이 깊어지니 정서 지원을 늘려주세요.", caution: true },
    { text: "아드님과는 사이가 소원하니, 먼저 그 얘기를 꺼내지 않는 게 좋아요.", caution: true },
  ],
};
