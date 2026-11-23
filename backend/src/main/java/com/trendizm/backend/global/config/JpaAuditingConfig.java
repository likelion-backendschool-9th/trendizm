package com.trendizm.backend.global.config;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseTimeEntity의 created_at, updated_at 자동 기록을 켠다.
 * 시각은 마이크로초까지만 쓴다. DB(PostgreSQL timestamp)는 마이크로초까지 저장하는데 리눅스 시계는 나노초까지 주므로,
 * 자르지 않으면 저장 직후 응답에 담긴 시각과 DB에 저장된 시각이 달라진다.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    }
}
