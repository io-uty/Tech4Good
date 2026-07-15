package com.tech4good.dolbom.portfolio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tech4good.dolbom.common.claude.ClaudeService;
import com.tech4good.dolbom.domain.Elder;
import com.tech4good.dolbom.domain.VisitLog;
import com.tech4good.dolbom.repository.ElderRepository;
import com.tech4good.dolbom.repository.VisitLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 기능2(AI 경력 포트폴리오 빌더).
 *
 * - stats, careHistoryByYear: LLM 없음, confirmed VisitLog만 집계
 * - careByElder: elderName(elders 조회)/period(최초 방문일)는 LLM 없음, summary만 Claude 배치 호출 1번
 * - timeline: Claude 호출 1번
 *
 * 담당 어르신 명단은 CareWorker/assignedElderIds가 아직 없어서, workerId의 confirmed
 * VisitLog에 등장하는 elderId distinct로 도출한다. CareWorker 쪽이 생기면 그걸로 바꾸는 게 더 정확하다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

	// 방문 1건당 고정 근사 시간(시간 단위). 정확한 체류시간 데이터가 없어 발표에서는 "추정치"라고 밝히면 충분.
	private static final double HOURS_PER_VISIT = 1.5;

	private final VisitLogRepository visitLogRepository;
	private final ElderRepository elderRepository;
	private final ClaudeService claudeService;

	public PortfolioResponse getPortfolio(String workerId) {
		List<VisitLog> confirmedLogs = visitLogRepository.findConfirmedByWorkerId(workerId);

		PortfolioStats stats = calculateStats(confirmedLogs);
		List<CareHistoryYear> careHistoryByYear = calculateCareHistoryByYear(confirmedLogs);

		List<CareByElder> careByElder;
		try {
			careByElder = generateCareByElder(confirmedLogs);
		} catch (Exception e) {
			log.warn("careByElder 생성 실패, 빈 리스트로 대체: {}", e.getMessage());
			careByElder = List.of();
		}

		List<TimelineEntry> timeline;
		try {
			timeline = generateTimeline(confirmedLogs);
		} catch (Exception e) {
			log.warn("timeline 생성 실패, 빈 리스트로 대체: {}", e.getMessage());
			timeline = List.of();
		}

		// TODO: improvementByYear - 이전 명세서 필드 구조가 아직 전달되지 않아 빈 리스트로 내려감
		List<Map<String, Object>> improvementByYear = List.of();

		return PortfolioResponse.builder()
				.workerId(workerId)
				.stats(stats)
				.careHistoryByYear(careHistoryByYear)
				.careByElder(careByElder)
				.improvementByYear(improvementByYear)
				.timeline(timeline)
				.build();
	}

	// ---------- LLM 없음 ----------

	private PortfolioStats calculateStats(List<VisitLog> confirmedLogs) {
		int totalCheckins = confirmedLogs.size();
		long elderCount = confirmedLogs.stream()
				.map(VisitLog::getElderId)
				.filter(id -> id != null && !id.isBlank())
				.collect(Collectors.toSet())
				.size();
		double totalHours = totalCheckins * HOURS_PER_VISIT;

		return PortfolioStats.builder()
				.totalCheckins(totalCheckins)
				.elderCount((int) elderCount)
				.totalHours(totalHours)
				.build();
	}

	private List<CareHistoryYear> calculateCareHistoryByYear(List<VisitLog> confirmedLogs) {
		Map<Integer, Set<String>> elderIdsByYear = new TreeMap<>();
		for (VisitLog visitLog : confirmedLogs) {
			LocalDate visitDate = parseDate(visitLog.getVisitDateTime());
			String elderId = visitLog.getElderId();
			if (visitDate == null || elderId == null || elderId.isBlank()) {
				continue;
			}
			elderIdsByYear.computeIfAbsent(visitDate.getYear(), y -> new HashSet<>()).add(elderId);
		}

		List<CareHistoryYear> result = new ArrayList<>();
		for (Map.Entry<Integer, Set<String>> entry : elderIdsByYear.entrySet()) {
			result.add(CareHistoryYear.builder()
					.year(entry.getKey())
					.elderCount(entry.getValue().size())
					.build());
		}
		return result;
	}

	// ---------- careByElder (elderName/period는 LLM 없음, summary만 Claude 배치 호출 1번) ----------

	private List<CareByElder> generateCareByElder(List<VisitLog> confirmedLogs) {
		Map<String, List<VisitLog>> logsByElder = confirmedLogs.stream()
				.filter(v -> v.getElderId() != null)
				.collect(Collectors.groupingBy(VisitLog::getElderId, LinkedHashMap::new, Collectors.toList()));

		if (logsByElder.isEmpty()) {
			return List.of();
		}

		Set<String> elderIds = logsByElder.keySet();

		Map<String, String> nameByElderId = elderRepository.findAllByIds(new ArrayList<>(elderIds)).stream()
				.collect(Collectors.toMap(Elder::getElderId, Elder::getName, (a, b) -> a));

		String userPrompt = PortfolioPrompts.buildCareByElderUserPrompt(elderIds, logsByElder);
		ElderSummaryBatchResponse response = claudeService.generateStructured(
				PortfolioPrompts.CARE_BY_ELDER_SYSTEM_PROMPT, userPrompt, ElderSummaryBatchResponse.class);

		Map<String, String> summaryByElder = response.getSummaries().stream()
				.collect(Collectors.toMap(ElderSummaryItem::getElderId, ElderSummaryItem::getSummary, (a, b) -> a));

		List<CareByElder> result = new ArrayList<>();
		for (String elderId : elderIds) {
			String name = nameByElderId.getOrDefault(elderId, elderId);
			LocalDate careStart = logsByElder.get(elderId).stream()
					.map(v -> parseDate(v.getVisitDateTime()))
					.filter(Objects::nonNull)
					.min(LocalDate::compareTo)
					.orElse(LocalDate.now());

			result.add(CareByElder.builder()
					.elderId(elderId)
					.elderName(name)
					.period(formatPeriod(careStart))
					.summary(summaryByElder.getOrDefault(elderId, ""))
					.build());
		}
		return result;
	}

	// ---------- timeline (Claude 호출 1번) ----------

	private List<TimelineEntry> generateTimeline(List<VisitLog> confirmedLogs) {
		if (confirmedLogs.isEmpty()) {
			return List.of();
		}
		String userPrompt = PortfolioPrompts.buildTimelineUserPrompt(confirmedLogs);
		TimelineResponse response = claudeService.generateStructured(
				PortfolioPrompts.TIMELINE_SYSTEM_PROMPT, userPrompt, TimelineResponse.class);
		return response.getEntries();
	}

	// ---------- 유틸 ----------

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

	/** 최초 방문일 기준 "2023.07 ~ 현재 (3년째)" 형식으로 변환 */
	private String formatPeriod(LocalDate careStart) {
		int years = Period.between(careStart, LocalDate.now()).getYears();
		int displayYears = years <= 0 ? 1 : years + 1; // 시작 연도 포함해서 "N년째"로 표기
		return String.format("%d.%02d ~ 현재 (%d년째)", careStart.getYear(), careStart.getMonthValue(), displayYears);
	}
}
