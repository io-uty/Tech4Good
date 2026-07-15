package com.tech4good.dolbom;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DolbomBackendApplication {

	public static void main(String[] args) {
		loadDotenv();
		SpringApplication.run(DolbomBackendApplication.class, args);
	}

	/**
	 * backend/.env 파일을 읽어 시스템 프로퍼티로 주입한다.
	 * 이미 OS 환경변수/시스템 프로퍼티로 설정된 키는 덮어쓰지 않는다.
	 */
	private static void loadDotenv() {
		Path[] candidates = { Path.of(".env"), Path.of("backend", ".env") };
		for (Path path : candidates) {
			if (!Files.isRegularFile(path)) {
				continue;
			}
			try {
				for (String line : Files.readAllLines(path)) {
					String trimmed = line.trim();
					if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
						continue;
					}
					int idx = trimmed.indexOf('=');
					String key = trimmed.substring(0, idx).trim();
					String value = trimmed.substring(idx + 1).trim();
					if (!value.isEmpty() && System.getenv(key) == null && System.getProperty(key) == null) {
						System.setProperty(key, value);
					}
				}
			} catch (IOException e) {
				System.err.println("Failed to read " + path + ": " + e.getMessage());
			}
			break;
		}
	}
}
