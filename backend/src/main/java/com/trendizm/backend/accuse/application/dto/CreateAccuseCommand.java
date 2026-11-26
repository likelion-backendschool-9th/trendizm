package com.trendizm.backend.accuse.application.dto;

/**
 * 신고 저장. 다른 도메인은 accuse의 enum을 import할 수 없어서 대상 종류·사유를 slug 문자열로 넘긴다.
 * target: "meme" 등 (AccuseTarget), reason: "sexual", "hate", "ad", "etc" (AccuseReason).
 * 대상이 실제로 있는지는 이 명령을 보내는 도메인이 먼저 확인한다.
 */
public record CreateAccuseCommand(
        String target,
        Long targetId,
        Long userId,
        String reason,
        String description
) {
}
