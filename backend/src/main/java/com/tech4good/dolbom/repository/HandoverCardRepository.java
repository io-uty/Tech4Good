package com.tech4good.dolbom.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.tech4good.dolbom.config.FirestoreProvider;
import com.tech4good.dolbom.domain.HandoverCard;

@Repository
public class HandoverCardRepository extends FirestoreSupport {

	private static final String COLLECTION = "handoverCards";

	private final FirestoreProvider firestoreProvider;

	public HandoverCardRepository(FirestoreProvider firestoreProvider, ObjectMapper objectMapper) {
		super(objectMapper);
		this.firestoreProvider = firestoreProvider;
	}

	private Firestore db() {
		return firestoreProvider.get();
	}

	/** 새 버전으로 저장 — 기존 문서를 덮어쓰지 않고 항상 새 문서를 만든다 (이전 버전 보존, CLAUDE.md 8.1절) */
	public void save(HandoverCard card) {
		await(db().collection(COLLECTION).document(card.getCardId()).set(toMap(card)));
	}

	public Optional<HandoverCard> findById(String cardId) {
		return fromSnapshot(await(db().collection(COLLECTION).document(cardId).get()), HandoverCard.class);
	}

	public Optional<HandoverCard> findLatestByElderId(String elderId) {
		return findAllByElderId(elderId).stream().findFirst();
	}

	/** 버전 내림차순 이력 조회 */
	public List<HandoverCard> findAllByElderId(String elderId) {
		var snapshot = await(db().collection(COLLECTION).whereEqualTo("elderId", elderId).get());
		return snapshot.getDocuments().stream()
				.map(doc -> objectMapper.convertValue(doc.getData(), HandoverCard.class))
				.sorted(Comparator.comparingInt(HandoverCard::getVersion).reversed())
				.toList();
	}
}
