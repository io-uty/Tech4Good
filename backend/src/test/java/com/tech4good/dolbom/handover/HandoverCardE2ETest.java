package com.tech4good.dolbom.handover;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.tech4good.dolbom.dev.DevSeedService;
import com.tech4good.dolbom.domain.HandoverCard;
import com.tech4good.dolbom.domain.HandoverSummary;
import com.tech4good.dolbom.domain.VisitLog;
import com.tech4good.dolbom.repository.HandoverCardRepository;
import com.tech4good.dolbom.repository.VisitLogRepository;

/**
 * 기능3 완료 기준(CLAUDE.md 8.5절) 검증 — 실제 Firestore + Claude API를 사용하는 E2E 테스트.
 * backend/.env에 ANTHROPIC_API_KEY, FIREBASE_CREDENTIALS_PATH가 없으면 자동 skip.
 *
 * 검증 항목:
 * 1. emotionalTriggers 각 항목에 sourceLogId가 2개 이상 연결
 * 2. 로그에 없는 병명/약물을 임의로 채우지 않음 (환각 방지)
 * 3. 재생성 시 이전 버전 보존 + previousVersionId 체인
 */
@SpringBootTest
class HandoverCardE2ETest {

	private static final String ELDER_ID = "elder-001";

	@Autowired
	DevSeedService devSeedService;
	@Autowired
	HandoverCardService handoverCardService;
	@Autowired
	HandoverCardRepository handoverCardRepository;
	@Autowired
	VisitLogRepository visitLogRepository;

	@BeforeAll
	static void loadEnvAndCheckKeys() throws IOException {
		for (Path path : new Path[] { Path.of(".env"), Path.of("backend", ".env") }) {
			if (!Files.isRegularFile(path)) {
				continue;
			}
			for (String line : Files.readAllLines(path)) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
					continue;
				}
				int idx = trimmed.indexOf('=');
				String key = trimmed.substring(0, idx).trim();
				String value = trimmed.substring(idx + 1).trim();
				if (!value.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, value);
				}
			}
			break;
		}
		Assumptions.assumeTrue(hasKey("ANTHROPIC_API_KEY") && hasKey("FIREBASE_CREDENTIALS_PATH"),
				"backend/.env에 ANTHROPIC_API_KEY / FIREBASE_CREDENTIALS_PATH가 없어 E2E 테스트를 건너뜁니다.");
	}

	private static boolean hasKey(String key) {
		String v = System.getProperty(key, System.getenv(key));
		return v != null && !v.isBlank();
	}

	@Test
	void 카드_생성_완료기준_검증() {
		devSeedService.seed();

		List<VisitLog> logs = visitLogRepository.findConfirmedByElderId(ELDER_ID);
		assertTrue(logs.size() >= 5, "완료 기준: 방문일지 5건 이상 더미 데이터");
		Set<String> validLogIds = logs.stream().map(VisitLog::getLogId).collect(Collectors.toSet());
		String corpus = logs.stream()
				.map(l -> l.getRawSttText() + " " + l.getStructuredLog().getElderCondition()
						+ " " + l.getStructuredLog().getSpecialNote())
				.collect(Collectors.joining(" "));

		// ---- 1차 생성 ----
		HandoverCard first = handoverCardService.generateDraft(ELDER_ID, "worker-001", "worker-002");
		HandoverSummary summary = first.getSummary();
		assertNotNull(summary, "summary가 생성되어야 함");

		// [기준 1] 정서 트리거: sourceLogIds 2개 이상 + 실제 존재하는 로그 ID만
		assertNotNull(summary.getEmotionalTriggers());
		assertFalse(summary.getEmotionalTriggers().isEmpty(),
				"반복 패턴(배우자 기일 우울, 비 오는 날 통증)이 심어져 있으므로 트리거가 1개 이상이어야 함");
		for (HandoverSummary.EmotionalTrigger trigger : summary.getEmotionalTriggers()) {
			Set<String> distinctIds = new HashSet<>(trigger.getSourceLogIds());
			assertTrue(distinctIds.size() >= 2,
					"트리거 [" + trigger.getTrigger() + "]의 sourceLogIds는 서로 다른 로그 2개 이상이어야 함: "
							+ trigger.getSourceLogIds());
			assertTrue(validLogIds.containsAll(distinctIds),
					"트리거 [" + trigger.getTrigger() + "]가 존재하지 않는 logId를 참조함: " + trigger.getSourceLogIds());
		}

		// [기준 2] 환각 방지: 병명/약물의 모든 토큰이 원본 로그 텍스트에 존재해야 함
		if (summary.getBasicInfo() != null) {
			assertGroundedInCorpus(summary.getBasicInfo().getChronicConditions(), corpus, "chronicConditions");
			assertGroundedInCorpus(summary.getBasicInfo().getMedications(), corpus, "medications");
		}

		// ---- 2차 생성 (재생성) ----
		HandoverCard second = handoverCardService.generateDraft(ELDER_ID, "worker-001", "worker-002");

		// [기준 3] 버전 체인: 이전 버전 보존 + previousVersionId 연결
		assertEquals(first.getVersion() + 1, second.getVersion(), "재생성 시 버전이 증가해야 함");
		assertEquals(first.getCardId(), second.getPreviousVersionId(),
				"previousVersionId가 직전 카드를 가리켜야 함");
		assertTrue(handoverCardRepository.findById(first.getCardId()).isPresent(),
				"재생성해도 이전 버전 카드가 삭제되지 않아야 함");
	}

	/** 목록의 각 항목을 토큰(2자 이상)으로 쪼개, 모든 토큰이 원본 로그에 등장하는지 검사 */
	private static void assertGroundedInCorpus(List<String> items, String corpus, String fieldName) {
		if (items == null) {
			return;
		}
		for (String item : items) {
			for (String token : item.split("[^가-힣a-zA-Z0-9]+")) {
				if (token.length() < 2) {
					continue;
				}
				assertTrue(corpus.contains(token),
						fieldName + " 항목 [" + item + "]의 토큰 [" + token + "]이 원본 로그에 없음 — 환각 의심");
			}
		}
	}
}
