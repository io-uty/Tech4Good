package com.tech4good.dolbom.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.DocumentSnapshot;

/**
 * Firestore 문서 <-> 도메인 객체 변환 유틸.
 * Firestore 기본 POJO 매핑 대신 Jackson을 사용해 Lombok 빌더 클래스와 호환.
 */
public abstract class FirestoreSupport {

	protected final ObjectMapper objectMapper;

	protected FirestoreSupport(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> toMap(Object entity) {
		return objectMapper.convertValue(entity, Map.class);
	}

	protected <T> Optional<T> fromSnapshot(DocumentSnapshot snapshot, Class<T> type) {
		if (snapshot == null || !snapshot.exists()) {
			return Optional.empty();
		}
		return Optional.of(objectMapper.convertValue(snapshot.getData(), type));
	}

	/** Firestore 비동기 API의 checked exception을 런타임 예외로 감싼다. */
	protected static <T> T await(com.google.api.core.ApiFuture<T> future) {
		try {
			return future.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Firestore 호출이 중단되었습니다", e);
		} catch (ExecutionException e) {
			throw new IllegalStateException("Firestore 호출 실패: " + e.getCause().getMessage(), e.getCause());
		}
	}
}
