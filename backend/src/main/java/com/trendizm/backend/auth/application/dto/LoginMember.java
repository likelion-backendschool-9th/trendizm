package com.trendizm.backend.auth.application.dto;

/** 로그인한 회원 정보. role은 "USER" 또는 "ADMIN" */
public record LoginMember(Long id, String nickname, String role) {
}
