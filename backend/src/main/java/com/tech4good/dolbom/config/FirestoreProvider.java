package com.tech4good.dolbom.config;

import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

/**
 * Firestore 지연 초기화 제공자.
 * 키 파일이 아직 준비되지 않아도 앱은 기동되고, 실제 DB 접근 시점에 명확한 오류를 낸다.
 */
@Component
public class FirestoreProvider {

	@Value("${firebase.credentials-path}")
	private String credentialsPath;

	private volatile Firestore firestore;

	public Firestore get() {
		if (firestore == null) {
			synchronized (this) {
				if (firestore == null) {
					firestore = init();
				}
			}
		}
		return firestore;
	}

	private Firestore init() {
		if (credentialsPath == null || credentialsPath.isBlank()) {
			throw new IllegalStateException(
					"FIREBASE_CREDENTIALS_PATH가 설정되지 않았습니다. backend/.env 파일에 서비스 계정 키 경로를 채워주세요.");
		}
		try (FileInputStream stream = new FileInputStream(credentialsPath)) {
			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseOptions options = FirebaseOptions.builder()
						.setCredentials(GoogleCredentials.fromStream(stream))
						.build();
				FirebaseApp.initializeApp(options);
			}
			return FirestoreClient.getFirestore();
		} catch (IOException e) {
			throw new IllegalStateException("Firebase 서비스 계정 키를 읽을 수 없습니다: " + credentialsPath, e);
		}
	}
}
