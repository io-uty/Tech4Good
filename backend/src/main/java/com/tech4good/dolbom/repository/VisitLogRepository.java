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

	/**
	 * 확정(confirmed)된 방문일지를 workerId 기준으로 시간순 정렬해 반환 — 기능2 포트폴리오 집계용.
	 * (findConfirmedByElderId와 완전히 같은 패턴, 필터 필드만 workerId로 다름)
	 */
	public List<VisitLog> findConfirmedByWorkerId(String workerId) {
		var snapshot = await(db().collection(COLLECTION).whereEqualTo("workerId", workerId).get());
		return snapshot.getDocuments().stream()
				.map(doc -> objectMapper.convertValue(doc.getData(), VisitLog.class))
				.filter(log -> "confirmed".equals(log.getStatus()))
				.sorted(Comparator.comparing(VisitLog::getVisitDateTime,
						Comparator.nullsFirst(Comparator.naturalOrder())))
				.toList();
	}

	/** 상태(draft/confirmed) 무관 전체 조회 — 기능2 logCompletionRate(일지 완료율) 계산용 */
	public List<VisitLog> findAllByWorkerId(String workerId) {
		var snapshot = await(db().collection(COLLECTION).whereEqualTo("workerId", workerId).get());
		return snapshot.getDocuments().stream()
				.map(doc -> objectMapper.convertValue(doc.getData(), VisitLog.class))
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

	/** logId 접두사(예: "log_20260715_")로 시작하는 문서 수를 센다 — 일자별 순번 채번용 */
	public int countByLogIdPrefix(String prefix) {
		var query = db().collection(COLLECTION)
				.orderBy(com.google.cloud.firestore.FieldPath.documentId())
				.startAt(prefix)
				.endAt(prefix + "\uf8ff");
		return await(query.get()).size();
	}
}
