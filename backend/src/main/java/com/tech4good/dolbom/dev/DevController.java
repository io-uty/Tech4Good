package com.tech4good.dolbom.dev;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/** 개발용 엔드포인트 — 배포 시 제거 또는 비활성화 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

	private final DevSeedService devSeedService;

	/** 더미 데이터 시딩 (멱등 — 여러 번 호출해도 동일 결과) */
	@PostMapping("/seed")
	public Map<String, Object> seed() {
		return devSeedService.seed();
	}
}
