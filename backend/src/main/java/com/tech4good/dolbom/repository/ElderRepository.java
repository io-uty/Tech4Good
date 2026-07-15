package com.tech4good.dolbom.repository;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.tech4good.dolbom.config.FirestoreProvider;
import com.tech4good.dolbom.domain.Elder;

@Repository
public class ElderRepository extends FirestoreSupport {

	private static final String COLLECTION = "elders";

	private final FirestoreProvider firestoreProvider;

	public ElderRepository(FirestoreProvider firestoreProvider, ObjectMapper objectMapper) {
		super(objectMapper);
		this.firestoreProvider = firestoreProvider;
	}

	private Firestore db() {
		return firestoreProvider.get();
	}

	public Optional<Elder> findById(String elderId) {
		return fromSnapshot(await(db().collection(COLLECTION).document(elderId).get()), Elder.class);
	}

	public void save(Elder elder) {
		await(db().collection(COLLECTION).document(elder.getElderId()).set(toMap(elder)));
	}

	/** elders 문서에는 최신 cardId만 참조로 저장 (CLAUDE.md 8.1절) */
	public void updateLatestCardId(String elderId, String cardId) {
		await(db().collection(COLLECTION).document(elderId)
				.update(Map.of("latestCardId", cardId)));
	}
}
