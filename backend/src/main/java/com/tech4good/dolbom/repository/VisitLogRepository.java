package com.tech4good.dolbom.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.tech4good.dolbom.config.FirestoreProvider;
import com.tech4good.dolbom.domain.VisitLog;

@Repository
public class VisitLogRepository extends FirestoreSupport {

	private static final String COLLECTION = "visitLogs";

	private final FirestoreProvider firestoreProvider;

	public VisitLogRepository(FirestoreProvider firestoreProvider, ObjectMapper objectMapper) {
		super(objectMapper);
		this.firestoreProvider = firestoreProvider;
	}

	private Firestore db() {
		return firestoreProvider.get();
	}

	/** 확정(confirmed)된 방문일지만 시간순 정렬해 반환 — 미확정 초안은 카드 생성에서 제외 (CLAUDE.md 8.2절) */
	public List<VisitLog> findConfirmedByElderId(String elderId) {
		var snapshot = await(db().collection(COLLECTION).whereEqualTo("elderId", elderId).get());
		return snapshot.getDocuments().stream()
				.map(doc -> objectMapper.convertValue(doc.getData(), VisitLog.class))
				.filter(log -> "confirmed".equals(log.getStatus()))
				.sorted(Comparator.comparing(VisitLog::getVisitDateTime,
						Comparator.nullsFirst(Comparator.naturalOrder())))
				.toList();
	}

	public Optional<VisitLog> findById(String logId) {
		return fromSnapshot(await(db().collection(COLLECTION).document(logId).get()), VisitLog.class);
	}

	public List<VisitLog> findByIds(List<String> logIds) {
		return logIds.stream()
				.map(this::findById)
				.flatMap(Optional::stream)
				.toList();
	}

	public void save(VisitLog log) {
		await(db().collection(COLLECTION).document(log.getLogId()).set(toMap(log)));
	}
}
