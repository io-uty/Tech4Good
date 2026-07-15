package com.tech4good.dolbom.handover;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tech4good.dolbom.domain.HandoverCard;
import com.tech4good.dolbom.domain.HandoverSummary;
import com.tech4good.dolbom.domain.VisitLog;

import lombok.RequiredArgsConstructor;

/** 기능3 인수인계 카드 API (CLAUDE.md 8.3절) */
@RestController
@RequestMapping("/api/handover-cards")
@RequiredArgsConstructor
public class HandoverCardController {

	private final HandoverCardService handoverCardService;

	public record GenerateRequest(String previousWorkerId, String newWorkerId) {
	}

	public record ConfirmRequest(HandoverSummary summary, String confirmedBy) {
	}

	/** 방문일지 기반 카드 초안 생성 (ADMIN 수동 트리거) */
	@PostMapping("/{elderId}/generate")
	public HandoverCard generate(@PathVariable String elderId,
			@RequestBody(required = false) GenerateRequest request) {
		String prev = request == null ? null : request.previousWorkerId();
		String next = request == null ? null : request.newWorkerId();
		return handoverCardService.generateDraft(elderId, prev, next);
	}

	/** 검토·수정 후 확정 */
	@PutMapping("/{cardId}/confirm")
	public HandoverCard confirm(@PathVariable String cardId, @RequestBody(required = false) ConfirmRequest request) {
		HandoverSummary edited = request == null ? null : request.summary();
		String confirmedBy = request == null ? null : request.confirmedBy();
		return handoverCardService.confirm(cardId, edited, confirmedBy);
	}

	/** 최신 카드 조회 */
	@GetMapping("/{elderId}/latest")
	public HandoverCard latest(@PathVariable String elderId) {
		return handoverCardService.getLatest(elderId);
	}

	/** 버전 이력 조회 */
	@GetMapping("/{elderId}/history")
	public List<HandoverCard> history(@PathVariable String elderId) {
		return handoverCardService.getHistory(elderId);
	}

	/** 카드 근거가 된 원본 로그 목록 (추적용) */
	@GetMapping("/{cardId}/source-logs")
	public List<VisitLog> sourceLogs(@PathVariable String cardId) {
		return handoverCardService.getSourceLogs(cardId);
	}
}
