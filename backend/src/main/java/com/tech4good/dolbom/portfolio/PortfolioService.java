package com.tech4good.dolbom.portfolio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tech4good.dolbom.common.claude.ClaudeService;
import com.tech4good.dolbom.domain.CareWorker;
import com.tech4good.dolbom.domain.Elder;
import com.tech4good.dolbom.domain.VisitLog;
import com.tech4good.dolbom.repository.CareWorkerRepository;
import com.tech4good.dolbom.repository.ElderRepository;
import com.tech4good.dolbom.repository.VisitLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기능2(AI 경력 포트폴리오 빌더) — 신규 응답 명세(근태/차트/자격증/경력 확장) 반영.
 *
 * - stats, attendanceStats, attendanceMonthly, activityTrends, carePerformances, experiences,
 *   certificates: LLM 없음. confirmed VisitLog 집계 + careWorkers 문서를 그대로/가공해 반환.
 *   attendanceMonthly·certificates·experiences는 실제 근태·인사 시스템이 없어 careWorkers에
 *   더미로 시드된 값을 쓴다(DevSeedService 참고) — 환각 방지 원칙에 따라 LLM이 지어내지 않는다.
 * - timeline: 신규배정/위험대응/근속마일스톤 후보를 규칙 기반으로 뽑아 Claude에 넘기고,
 *   그중 의미 있는 사건을 골라 문구만 다듬게 한다(후보에 없는 사건 생성 금지).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

	// 방문 1건당 고정 근사 시간(시간 단위). 정확한 체류시간 데이터가 없어 발표에서는 "추정치"라고 밝히면 충분.
	private static final double HOURS_PER_VISIT = 1.5;
	private static final int TENURE_MILESTONE_STEP_MONTHS = 6;

	private final VisitLogRepository visitLogRepository;
	private final ElderRepository elderRepository;
	private final CareWorkerRepository careWorkerRepository;
	private final ClaudeService claudeService;

	public PortfolioResponse getPortfolio(String workerId) {
		CareWorker careWorker = careWorkerRepository.findById(workerId).orElseGet(() -> emptyCareWorker(workerId));
		List<VisitLog> confirmedLogs = visitLogRepository.findConfirmedByWorkerId(workerId);
		List<VisitLog> allLogs = visitLogRepository.findAllByWorkerId(workerId);

		PortfolioStats stats = calculateStats(confirmedLogs, careWorker);
		AttendanceStats attendanceStats = calculateAttendanceStats(careWorker, allLogs);
		List<AttendanceMonthlyEntry> attendanceMonthly = mapAttendanceMonthly(careWorker);
		List<ActivityTrendEntry> activityTrends = calculateActivityTrends(confirmedLogs);
		List<CarePerformanceItem> carePerformances = calculateCarePerformances(confirmedLogs);
		List<ExperienceItem> experiences = mapExperiences(careWorker);
		List<CertificateItem> certificates = mapCertificates(careWorker);

		List<TimelineEntry> timeline;
		try {
			timeline = generateTimeline(careWorker, confirmedLogs);
		} catch (Exception e) {
			log.warn("timeline 생성 실패, 빈 리스트로 대체: {}", e.getMessage());
			timeline = List.of();
		}

		return PortfolioResponse.builder()
				.workerId(workerId)
				.stats(stats)
				.attendanceStats(attendanceStats)
				.attendanceMonthly(attendanceMonthly)
				.activityTrends(activityTrends)
				.carePerformances(carePerformances)
				.experiences(experiences)
				.certificates(certificates)
				.timeline(timeline)
				.build();
	}

	// ---------- LLM 없음 ----------

	private PortfolioStats calculateStats(List<VisitLog> confirmedLogs, CareWorker careWorker) {
		int totalCheckins = confirmedLogs.size();
		long elderCount = confirmedLogs.stream()
				.map(VisitLog::getElderId)
				.filter(id -> id != null && !id.isBlank())
				.collect(Collectors.toSet())
				.size();
		double totalHours = totalCheckins * HOURS_PER_VISIT;

		int attendance = 0;
		int late = 0;
		int absence = 0;
		for (CareWorker.MonthlyAttendance m : nullToEmpty(careWorker.getAttendanceMonthly())) {
			attendance += m.getAttendance();
			late += m.getLate();
			absence += m.getAbsence();
		}
		int denom = attendance + late + absence;
		double attendanceRate = denom == 0 ? 0.0 : round1(attendance * 100.0 / denom);

		return PortfolioStats.builder()
				.totalCheckins(totalCheckins)
				.totalHours(totalHours)
				.elderCount((int) elderCount)
				.attendanceRate(attendanceRate)
				.build();
	}

	private AttendanceStats calculateAttendanceStats(CareWorker careWorker, List<VisitLog> allLogs) {
		int totalWorkDays = 0;
		int absence = 0;
		int late = 0;
		for (CareWorker.MonthlyAttendance m : nullToEmpty(careWorker.getAttendanceMonthly())) {
			totalWorkDays += m.getAttendance() + m.getLate();
			absence += m.getAbsence();
			late += m.getLate();
		}

		long confirmedCount = allLogs.stream().filter(l -> "confirmed".equals(l.getStatus())).count();
		double logCompletionRate = allLogs.isEmpty() ? 0.0 : round1(confirmedCount * 100.0 / allLogs.size());

		return AttendanceStats.builder()
				.totalWorkDays(totalWorkDays)
				.absence(absence)
				.late(late)
				.logCompletionRate(logCompletionRate)
				.build();
	}

	private List<AttendanceMonthlyEntry> mapAttendanceMonthly(CareWorker careWorker) {
		return nullToEmpty(careWorker.getAttendanceMonthly()).stream()
				.map(m -> AttendanceMonthlyEntry.builder()
						.month(m.getMonth())
						.attendance(m.getAttendance())
						.late(m.getLate())
						.absence(m.getAbsence())
						.vacation(m.getVacation())
						.build())
				.toList();
	}

	private List<ActivityTrendEntry> calculateActivityTrends(List<VisitLog> confirmedLogs) {
		Map<Integer, List<VisitLog>> logsByMonth = new TreeMap<>();
		for (VisitLog visitLog : confirmedLogs) {
			LocalDate date = parseDate(visitLog.getVisitDateTime());
			if (date == null) {
				continue;
			}
			// 연+월 순서 보장을 위해 "yyyyMM" 정수 키 사용
			int key = date.getYear() * 100 + date.getMonthValue();
			logsByMonth.computeIfAbsent(key, k -> new ArrayList<>()).add(visitLog);
		}

		List<ActivityTrendEntry> result = new ArrayList<>();
		for (Map.Entry<Integer, List<VisitLog>> entry : logsByMonth.entrySet()) {
			int monthValue = entry.getKey() % 100;
			List<VisitLog> monthLogs = entry.getValue();
			long elders = monthLogs.stream().map(VisitLog::getElderId).filter(id -> id != null && !id.isBlank())
					.collect(Collectors.toSet()).size();
			result.add(ActivityTrendEntry.builder()
					.month(monthValue + "월")
					.visits(monthLogs.size())
					.hours(monthLogs.size() * HOURS_PER_VISIT)
					.elders((int) elders)
					.build());
		}
		return result;
	}

	private List<CarePerformanceItem> calculateCarePerformances(List<VisitLog> confirmedLogs) {
		Map<String, Long> byServiceType = confirmedLogs.stream()
				.map(VisitLog::getStructuredLog)
				.filter(s -> s != null && s.getServiceType() != null)
				.collect(Collectors.groupingBy(VisitLog.StructuredLog::getServiceType, Collectors.counting()));

		List<CarePerformanceItem> result = new ArrayList<>();
		addPerformanceIfPresent(result, byServiceType, "안전지원", "safety", "안전 지원", "emergency");
		addPerformanceIfPresent(result, byServiceType, "사회참여", "social", "사회 참여", "emotion");
		addPerformanceIfPresent(result, byServiceType, "일상생활지원", "daily", "일상생활 지원", "food");

		long riskCount = confirmedLogs.stream()
				.filter(l -> l.getRiskTags() != null && !l.getRiskTags().isEmpty())
				.count();
		if (riskCount > 0) {
			result.add(CarePerformanceItem.builder()
					.id("risk").label("위험 신호 대응").value(riskCount + "회").iconType("hospital").build());
		}
		return result;
	}

	private void addPerformanceIfPresent(List<CarePerformanceItem> result, Map<String, Long> byServiceType,
			String serviceType, String id, String label, String iconType) {
		Long count = byServiceType.get(serviceType);
		if (count != null && count > 0) {
			result.add(CarePerformanceItem.builder().id(id).label(label).value(count + "회").iconType(iconType).build());
		}
	}

	private List<ExperienceItem> mapExperiences(CareWorker careWorker) {
		return nullToEmpty(careWorker.getExperiences()).stream()
				.map(e -> ExperienceItem.builder().period(e.getPeriod()).title(e.getTitle()).isActive(e.isActive()).build())
				.toList();
	}

	private List<CertificateItem> mapCertificates(CareWorker careWorker) {
		return nullToEmpty(careWorker.getCertificates()).stream()
				.map(c -> CertificateItem.builder().title(c.getTitle()).date(c.getDate()).build())
				.toList();
	}

	// ---------- timeline (후보는 규칙 기반, 문구 다듬기+선별만 Claude 1회 호출) ----------

	private List<TimelineEntry> generateTimeline(CareWorker careWorker, List<VisitLog> confirmedLogs) {
		List<PortfolioPrompts.MilestoneCandidate> candidates = buildMilestoneCandidates(careWorker, confirmedLogs);
		if (candidates.isEmpty()) {
			return List.of();
		}
		String userPrompt = PortfolioPrompts.buildTimelineUserPrompt(candidates);
		TimelineResponse response = claudeService.generateStructured(
				PortfolioPrompts.TIMELINE_SYSTEM_PROMPT, userPrompt, TimelineResponse.class);
		return response.getEntries();
	}

	private List<PortfolioPrompts.MilestoneCandidate> buildMilestoneCandidates(
			CareWorker careWorker, List<VisitLog> confirmedLogs) {
		List<PortfolioPrompts.MilestoneCandidate> candidates = new ArrayList<>();

		Set<String> elderIds = confirmedLogs.stream().map(VisitLog::getElderId)
				.filter(id -> id != null && !id.isBlank()).collect(Collectors.toSet());
		Map<String, String> nameByElderId = elderRepository.findAllByIds(new ArrayList<>(elderIds)).stream()
				.collect(Collectors.toMap(Elder::getElderId, Elder::getName, (a, b) -> a));

		// 신규 배정: 어르신별 최초 confirmed 방문일
		Map<String, VisitLog> firstLogByElder = confirmedLogs.stream()
				.filter(l -> l.getElderId() != null && parseDate(l.getVisitDateTime()) != null)
				.collect(Collectors.toMap(VisitLog::getElderId, l -> l,
						(a, b) -> parseDate(a.getVisitDateTime()).isBefore(parseDate(b.getVisitDateTime())) ? a : b));
		for (VisitLog first : firstLogByElder.values()) {
			String name = nameByElderId.getOrDefault(first.getElderId(), first.getElderId());
			candidates.add(new PortfolioPrompts.MilestoneCandidate(
					dateOnly(first.getVisitDateTime()), "신규배정", "user",
					name + " 어르신 담당 시작 (첫 방문일 기준)"));
		}

		// 위험 대응: riskTags가 있는 로그
		for (VisitLog l : confirmedLogs) {
			if (l.getRiskTags() == null || l.getRiskTags().isEmpty()) {
				continue;
			}
			String name = nameByElderId.getOrDefault(l.getElderId(), l.getElderId());
			String specialNote = l.getStructuredLog() != null ? l.getStructuredLog().getSpecialNote() : null;
			candidates.add(new PortfolioPrompts.MilestoneCandidate(
					dateOnly(l.getVisitDateTime()), "위험대응", "hospital",
					name + " 어르신 - 위험 신호: " + String.join(", ", l.getRiskTags())
							+ (specialNote != null ? " / 특이사항: " + specialNote : "")));
		}

		// 근속 마일스톤: hireDate 기준 6개월 단위
		String hireDate = careWorker.getHireDate();
		if (hireDate != null && !hireDate.isBlank()) {
			try {
				LocalDate start = LocalDate.parse(hireDate);
				long elapsedMonths = ChronoUnit.MONTHS.between(start, LocalDate.now());
				for (long m = TENURE_MILESTONE_STEP_MONTHS; m <= elapsedMonths; m += TENURE_MILESTONE_STEP_MONTHS) {
					LocalDate milestoneDate = start.plusMonths(m);
					candidates.add(new PortfolioPrompts.MilestoneCandidate(
							milestoneDate.toString(), "근속마일스톤", "badge",
							"근속 " + m + "개월 달성 (" + careWorker.getOrg() + ")"));
				}
			} catch (Exception e) {
				log.warn("hireDate 파싱 실패, 근속 마일스톤 후보 생략: {}", hireDate);
			}
		}

		return candidates.stream()
				.sorted(Comparator.comparing(PortfolioPrompts.MilestoneCandidate::date,
						Comparator.nullsFirst(Comparator.naturalOrder())))
				.toList();
	}

	// ---------- 유틸 ----------

	private CareWorker emptyCareWorker(String workerId) {
		return CareWorker.builder()
				.workerId(workerId)
				.assignedElderIds(List.of())
				.certificates(List.of())
				.experiences(List.of())
				.attendanceMonthly(List.of())
				.build();
	}

	private <T> List<T> nullToEmpty(List<T> list) {
		return list == null ? List.of() : list;
	}

	/** VisitLog.visitDateTime은 "2026-03-05T10:30:00" 같은 ISO-8601 문자열이라고 가정 */
	private LocalDate parseDate(String isoDateTime) {
		if (isoDateTime == null || isoDateTime.isBlank()) {
			return null;
		}
		try {
			return LocalDateTime.parse(isoDateTime).toLocalDate();
		} catch (Exception e) {
			return null;
		}
	}

	private String dateOnly(String isoDateTime) {
		LocalDate date = parseDate(isoDateTime);
		return date == null ? isoDateTime : date.toString();
	}

	private double round1(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
