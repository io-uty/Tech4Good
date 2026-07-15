package com.tech4good.dolbom.dev;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tech4good.dolbom.domain.Elder;
import com.tech4good.dolbom.domain.VisitLog;
import com.tech4good.dolbom.repository.ElderRepository;
import com.tech4good.dolbom.repository.VisitLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 개발용 더미 데이터 시더. 어르신 3명 + 확정 방문일지 20건.
 * 고정 문서 ID를 사용하므로 여러 번 실행해도 안전(멱등).
 *
 * 검증 포인트가 데이터에 심어져 있다:
 * - 반복 정서 트리거(로그 2건 이상): 김순자-배우자 기일 우울(log-004,005), 비 오는 날 무릎통증·기분저하(log-002,007)
 *   박철수-사별한 아내 언급 시 침울(log-010,013), 이복례-반려견 죽음 언급 시 눈물(log-016,019)
 * - 1회성 언급(트리거로 잡히면 안 됨): 딸 통화 서운함(log-008), 옆집 소음(log-012), 보이스피싱 불안(log-018)
 * - 명시된 지병/약물만 카드에 나와야 함: 고혈압/당뇨/암로디핀/메트포르민(김순자), 관절염(박철수), 고지혈증(이복례)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DevSeedService {

	private final ElderRepository elderRepository;
	private final VisitLogRepository visitLogRepository;

	public Map<String, Object> seed() {
		String now = LocalDateTime.now().toString();

		List<Elder> elders = List.of(
				Elder.builder().elderId("elder-001").name("김순자").birthDate("1948-05-20")
						.address("경남 창원시 의창구").guardianContact("010-1111-2222")
						.createdAt(now).updatedAt(now).build(),
				Elder.builder().elderId("elder-002").name("박철수").birthDate("1944-11-03")
						.address("경남 창원시 성산구").guardianContact("010-3333-4444")
						.createdAt(now).updatedAt(now).build(),
				Elder.builder().elderId("elder-003").name("이복례").birthDate("1951-02-14")
						.address("경남 창원시 마산합포구").guardianContact("010-5555-6666")
						.createdAt(now).updatedAt(now).build());
		elders.forEach(elderRepository::save);

		List<VisitLog> logs = List.of(
				// ===== elder-001 김순자 (8건) =====
				log("log-001", "elder-001", "2026-01-10T10:30:00", "일상생활지원",
						"밑반찬 전달 및 청소 지원",
						"컨디션 양호. 혈압약(암로디핀) 잘 복용하고 계심.",
						"베란다 화초 가꾸는 것을 자랑하시며 매우 즐거워하심. 트로트 방송을 즐겨 들으신다고 함."),
				log("log-002", "elder-001", "2026-02-07T11:00:00", "안전지원",
						"주 1회 안부 확인 방문",
						"비 오는 날이라 무릎 통증이 심하다고 호소. 기분도 가라앉아 계심. 당뇨약(메트포르민) 복용 확인.",
						"비가 오면 무릎이 쑤셔서 아무것도 하기 싫다고 하심."),
				log("log-003", "elder-001", "2026-02-21T14:00:00", "사회참여",
						"경로당 동행",
						"전반적으로 양호하나 대화 중 감정 기복 관찰.",
						"경로당에서 다른 어르신이 아들 사업 이야기를 꺼내자 말수가 줄고 표정이 어두워지심. 아들 사업 실패 이후 그 이야기를 꺼리시는 듯함."),
				log("log-004", "elder-001", "2026-03-05T10:30:00", "안전지원",
						"안부 확인 및 식사 상태 점검",
						"식사량이 눈에 띄게 줄었음. 우울감 호소.",
						"3월 12일 배우자 기일이 다가오면서 마음이 힘들다고 하심. 배우자 사진을 보며 눈물 보이심."),
				log("log-005", "elder-001", "2026-03-19T10:30:00", "일상생활지원",
						"가사 지원 및 말벗",
						"기일이 지난 후에도 우울감이 지속됨. 밤에 잠을 잘 못 잔다고 하심.",
						"매년 3월 기일 전후로 힘들다고 직접 말씀하심. 작년에도 이맘때 많이 힘들었다고 함.",
						List.of("우울감 지속", "수면장애")),
				log("log-006", "elder-001", "2026-04-16T13:30:00", "사회참여",
						"복지관 원예 프로그램 동행",
						"표정이 매우 밝고 활기참.",
						"원예 프로그램에서 화초 이야기를 하며 신나 하심. 프로그램 계속 참여를 원하심."),
				log("log-007", "elder-001", "2026-05-14T11:00:00", "안전지원",
						"우천 시 안부 확인",
						"비가 와서 무릎 통증이 심해 외출을 못 하셨음. 기분이 가라앉아 있음.",
						"지난번처럼 비 오는 날엔 무릎이 아파 집에만 있게 되고 우울해진다고 하심."),
				log("log-008", "elder-001", "2026-06-11T10:30:00", "일상생활지원",
						"밑반찬 전달 및 청소 지원",
						"혈압약 잘 복용 중. 전반적으로 안정적.",
						"어제 딸과 통화 후 서운했다고 한 번 말씀하심. 오후에는 트로트 프로그램을 보며 기분 회복하심."),

				// ===== elder-002 박철수 (6건) =====
				log("log-009", "elder-002", "2026-01-15T10:00:00", "안전지원",
						"주 1회 안부 확인",
						"관절염으로 무릎이 불편하나 거동 가능. 진통제 처방받아 복용 중.",
						"6·25 참전 시절 이야기를 시작하면 한 시간 넘게 즐겁게 말씀하심. 군대 이야기를 매우 좋아하심."),
				log("log-010", "elder-002", "2026-02-12T10:00:00", "일상생활지원",
						"장보기 동행",
						"거동 양호. 대화 중 감정 변화 관찰.",
						"시장에서 아내가 좋아하던 생선 가게를 지나며 사별한 아내 이야기가 나오자 말씀이 없어지고 침울해지심."),
				log("log-011", "elder-002", "2026-03-11T14:00:00", "사회참여",
						"보훈회관 행사 동행",
						"컨디션 양호. 행사 내내 활기참.",
						"참전용사 모임에서 옛 전우들과 군대 이야기를 나누며 매우 즐거워하심."),
				log("log-012", "elder-002", "2026-04-09T10:00:00", "안전지원",
						"안부 확인",
						"양호. 관절염 통증은 평소 수준.",
						"옆집 공사 소음 때문에 요즘 짜증이 난다고 한 번 언급하심."),
				log("log-013", "elder-002", "2026-05-21T10:00:00", "일상생활지원",
						"가사 지원",
						"식사는 잘 하시나 기분이 가라앉아 있음.",
						"결혼기념일이었다며 사별한 아내 사진을 꺼내 보시고 하루 종일 침울해하심. 아내 이야기가 나오면 말씀을 잘 잇지 못하심."),
				log("log-014", "elder-002", "2026-06-18T10:00:00", "안전지원",
						"폭염 대비 안부 확인",
						"양호. 선풍기 사용 중이며 수분 섭취 안내함.",
						"손자가 놀러 온다고 들떠 계심. 군대 시절 무용담을 들려줄 거라고 하심."),

				// ===== elder-003 이복례 (6건) =====
				log("log-015", "elder-003", "2026-01-20T13:00:00", "안전지원",
						"주 1회 안부 확인",
						"청력이 많이 저하되어 보청기 착용 중. 크고 또렷하게 말해야 대화 가능. 고지혈증약 복용 중.",
						"교회 성가대 활동 이야기를 하실 때 표정이 밝아지심."),
				log("log-016", "elder-003", "2026-02-17T13:00:00", "일상생활지원",
						"병원 동행(정기 검진)",
						"검진 결과 특이사항 없음. 귀가 중 감정 변화 관찰.",
						"길에서 강아지를 보고 작년에 떠나보낸 반려견 복실이가 생각난다며 눈물을 보이심."),
				log("log-017", "elder-003", "2026-03-16T13:00:00", "사회참여",
						"교회 행사 동행",
						"컨디션 양호.",
						"성가대 발표를 마치고 매우 뿌듯해하심. 교회 사람들 이야기를 즐겁게 하심."),
				log("log-018", "elder-003", "2026-04-13T13:00:00", "안전지원",
						"안부 확인",
						"보이스피싱 의심 전화를 받고 불안해하셨음. 대처 방법 안내함.",
						"모르는 번호로 아들을 사칭한 전화가 와서 놀라셨다고 한 번 말씀하심.",
						List.of("보이스피싱 노출")),
				log("log-019", "elder-003", "2026-05-18T13:00:00", "일상생활지원",
						"가사 지원 및 말벗",
						"양호하나 대화 중 눈물 보이심.",
						"TV에 강아지가 나오자 복실이 이야기를 하며 또 눈물을 흘리심. 복실이 이야기가 나오면 한동안 말을 잇지 못하심."),
				log("log-020", "elder-003", "2026-06-15T13:00:00", "안전지원",
						"폭염 대비 안부 확인",
						"양호. 보청기 배터리 교체 지원.",
						"다음 주 교회 야유회를 기대하고 계심."));
		logs.forEach(visitLogRepository::save);

		log.info("더미 데이터 시딩 완료: elders={}, visitLogs={}", elders.size(), logs.size());
		return Map.of(
				"elders", elders.stream().map(Elder::getElderId).toList(),
				"visitLogCount", logs.size());
	}

	private static VisitLog log(String logId, String elderId, String dateTime, String serviceType,
			String activity, String condition, String specialNote) {
		return log(logId, elderId, dateTime, serviceType, activity, condition, specialNote, List.of());
	}

	private static VisitLog log(String logId, String elderId, String dateTime, String serviceType,
			String activity, String condition, String specialNote, List<String> riskTags) {
		return VisitLog.builder()
				.logId(logId)
				.elderId(elderId)
				.workerId("worker-001")
				.visitDateTime(dateTime)
				.rawSttText("(음성 메모) " + activity + " " + condition + " " + specialNote)
				.structuredLog(VisitLog.StructuredLog.builder()
						.serviceType(serviceType)
						.activityDetail(activity)
						.elderCondition(condition)
						.specialNote(specialNote)
						.build())
				.riskTags(riskTags)
				.status("confirmed")
				.confirmedBy("worker-001")
				.confirmedAt(dateTime)
				.build();
	}
}
