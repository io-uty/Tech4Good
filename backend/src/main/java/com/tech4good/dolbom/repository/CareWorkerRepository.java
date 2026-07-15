package com.tech4good.dolbom.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.tech4good.dolbom.config.FirestoreProvider;
import com.tech4good.dolbom.domain.CareWorker;

@Repository
public class CareWorkerRepository extends FirestoreSupport {

	private static final String COLLECTION = "careWorkers";

	private final FirestoreProvider firestoreProvider;

	public CareWorkerRepository(FirestoreProvider firestoreProvider, ObjectMapper objectMapper) {
		super(objectMapper);
		this.firestoreProvider = firestoreProvider;
	}

	private Firestore db() {
		return firestoreProvider.get();
	}

	public Optional<CareWorker> findById(String workerId) {
		return fromSnapshot(await(db().collection(COLLECTION).document(workerId).get()), CareWorker.class);
	}

	public void save(CareWorker careWorker) {
		await(db().collection(COLLECTION).document(careWorker.getWorkerId()).set(toMap(careWorker)));
	}
}
