package com.trendizm.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @Async를 켠다. 실행기는 Spring Boot가 만드는 기본 applicationTaskExecutor를 쓴다.
 * 쓰는 곳: 밈 문서 썸네일 저장(lookup MemeThumbnailServiceImpl)
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
