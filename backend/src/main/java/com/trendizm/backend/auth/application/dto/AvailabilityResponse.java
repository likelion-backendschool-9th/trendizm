package com.trendizm.backend.auth.application.dto;

/** 이메일·닉네임 중복 확인 결과. 예: { "available": true } */
public record AvailabilityResponse(boolean available) {
}
