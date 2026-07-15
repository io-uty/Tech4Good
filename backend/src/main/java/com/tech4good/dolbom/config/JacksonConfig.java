package com.tech4good.dolbom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Firestore 문서 매핑용 Jackson 2 ObjectMapper.
 * (Spring Boot 4의 웹 직렬화는 Jackson 3를 쓰므로 별도 빈으로 등록)
 */
@Configuration
public class JacksonConfig {

	@Bean
	public ObjectMapper firestoreObjectMapper() {
		return new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
}
