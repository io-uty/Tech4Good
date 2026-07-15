package com.tech4good.dolbom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DolbomBackendApplicationTests {

	// 테스트 클래스 실행 순서에 따라 Spring 컨텍스트가 먼저 캐싱되면서 .env 값이 반영 안 되는 문제를 막기 위해,
	// 다른 테스트(HandoverCardE2ETest)와 동일하게 컨텍스트 로드 전에 .env를 읽어 System property로 주입한다.
	@BeforeAll
	static void loadDotenv() throws IOException {
		for (Path path : new Path[] { Path.of(".env"), Path.of("backend", ".env") }) {
			if (!Files.isRegularFile(path)) {
				continue;
			}
			for (String line : Files.readAllLines(path)) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
					continue;
				}
				int idx = trimmed.indexOf('=');
				String key = trimmed.substring(0, idx).trim();
				String value = trimmed.substring(idx + 1).trim();
				if (!value.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
					System.setProperty(key, value);
				}
			}
			break;
		}
	}

	@Test
	void contextLoads() {
	}

}
