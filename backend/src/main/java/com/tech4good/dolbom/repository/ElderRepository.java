package com.tech4good.dolbom.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
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

	/** elderId 목록에 해당하는 elders 문서를 한 번에 배치 조회 — 기능2 careByElder에서 씀 */
	public List<Elder> findAllByIds(List<String> elderIds) {
		if (elderIds == null || elderIds.isEmpty()) {
			return List.of();
		}
		DocumentReference[] refs = elderIds.stream()
				.map(id -> db().collection(COLLECTION).document(id))
				.toArray(DocumentReference[]::new);

		List<DocumentSnapshot> docs = await(db().getAll(refs));
		return docs.stream()
				.map(doc -> fromSnapshot(doc, Elder.class))
				.flatMap(Optional::stream)
				.toList();
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
