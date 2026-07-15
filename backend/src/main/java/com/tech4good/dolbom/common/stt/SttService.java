package com.tech4good.dolbom.common.stt;

/** STT(음성-텍스트 변환) 호출을 감싸는 공통 서비스 계층. */
public interface SttService {

	/**
	 * @param audioBytes  오디오 파일 바이너리 (wav/mp3 등)
	 * @param contentType 업로드된 파일의 Content-Type (예: audio/wav, audio/mpeg)
	 * @return STT 변환된 텍스트 원문
	 */
	String transcribe(byte[] audioBytes, String contentType);
}
