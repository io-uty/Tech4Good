package com.tech4good.dolbom.handover;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tech4good.dolbom.common.ApiExceptions.BadRequestException;
import com.tech4good.dolbom.common.ApiExceptions.NotFoundException;
import com.tech4good.dolbom.common.claude.ClaudeService;
import com.tech4good.dolbom.domain.Elder;
import com.tech4good.dolbom.domain.HandoverCard;
import com.tech4good.dolbom.domain.HandoverSummary;
import com.tech4good.dolbom.domain.VisitLog;
import com.tech4good.dolbom.repository.ElderRepository;
import com.tech4good.dolbom.repository.HandoverCardRepository;
import com.tech4good.dolbom.repository.VisitLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandoverCardService {

	private final ElderRepository elderRepository;
	private final VisitLogRepository visitLogRepository;
	private final HandoverCardRepository handoverCardRepository;
	private final ClaudeService claudeService;

	/** 확정 방문일지 기반 카드 초안 생성 (CLAUDE.md 8.2절 파이프라인) */
	public HandoverCard generateDraft(String elderId, String previousWorkerId, String newWorkerId) {
		Elder elder = elderRepository.findById(elderId)
				.orElseThrow(() -> new NotFoundException("어르신을 찾을 수 없습니다: " + elderId));

		List<VisitLog> logs = visitLogRepository.findConfirmedByElderId(elderId);
		if (logs.isEmpty()) {
			throw new BadRequestException("확정(confirmed)된 방문일지가 없어 카드를 생성할 수 없습니다: " + elderId);
		}

		String userPrompt = HandoverPrompts.buildUserPrompt(elder.getName(), logs);
		HandoverSummary summary = claudeService.generateStructured(
				HandoverPrompts.SYSTEM_PROMPT, userPrompt, HandoverSummary.class);

		HandoverCard previous = handoverCardRepository.findLatestByElderId(elderId).orElse(null);

		HandoverCard card = HandoverCard.builder()
				.cardId("card-" + UUID.randomUUID())
				.elderId(elderId)
				.generatedAt(LocalDateTime.now().toString())
				.previousWorkerId(previousWorkerId)
				.newWorkerId(newWorkerId)
				.sourceLogRange(HandoverCard.SourceLogRange.builder()
						.fromDate(logs.get(0).getVisitDateTime())
						.toDate(logs.get(logs.size() - 1).getVisitDateTime())
						.logCount(logs.size())
						.build())
				.sourceLogIds(logs.stream().map(VisitLog::getLogId).toList())
				.summary(summary)
				.version(previous == null ? 1 : previous.getVersion() + 1)
				.previousVersionId(previous == null ? null : previous.getCardId())
				.status("draft")
				.build();

		handoverCardRepository.save(card);
		log.info("인수인계 카드 초안 생성: elderId={}, cardId={}, version={}", elderId, card.getCardId(), card.getVersion());
		return card;
	}

	/** 검토·수정 후 확정 저장 + elders 최신 카드 참조 갱신 */
	public HandoverCard confirm(String cardId, HandoverSummary editedSummary, String confirmedBy) {
		HandoverCard card = handoverCardRepository.findById(cardId)
				.orElseThrow(() -> new NotFoundException("카드를 찾을 수 없습니다: " + cardId));

		if (editedSummary != null) {
			card.setSummary(editedSummary);
		}
		card.setStatus("confirmed");
		card.setConfirmedBy(confirmedBy);
		card.setConfirmedAt(LocalDateTime.now().toString());
		handoverCardRepository.save(card);
		elderRepository.updateLatestCardId(card.getElderId(), card.getCardId());
		return card;
	}

	public HandoverCard getLatest(String elderId) {
		return handoverCardRepository.findLatestByElderId(elderId)
				.orElseThrow(() -> new NotFoundException("생성된 카드가 없습니다: " + elderId));
	}

	public List<HandoverCard> getHistory(String elderId) {
		return handoverCardRepository.findAllByElderId(elderId);
	}

	/** 카드 근거가 된 원본 방문일지 목록 (추적용) */
	public List<VisitLog> getSourceLogs(String cardId) {
		HandoverCard card = handoverCardRepository.findById(cardId)
				.orElseThrow(() -> new NotFoundException("카드를 찾을 수 없습니다: " + cardId));
		return visitLogRepository.findByIds(
				card.getSourceLogIds() == null ? List.of() : card.getSourceLogIds());
	}
}
