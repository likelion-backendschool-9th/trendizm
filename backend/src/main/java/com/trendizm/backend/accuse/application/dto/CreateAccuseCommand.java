package com.trendizm.backend.accuse.application.dto;

/**
 * 신고 저장. target은 신고 대상 엔티티의 테이블 이름 상수(예: Meme.TABLE_NAME = "memes")를 넘긴다. 손으로 문자열을 쓰지 않는다.
 * reason은 사유 slug: "sexual", "hate", "ad", "false_info", "etc" (다른 도메인은 accuse의 enum을 import할 수 없다).
 * 대상이 실제로 있는지는 이 명령을 보내는 도메인이 먼저 확인하고, 그때 읽은 대상의 작성자 id를 targetOwnerId로 넘긴다
 * (자기 글 신고 검사용, 저장하지 않는다).
 */
public record CreateAccuseCommand(
        String target,
        Long targetId,
        Long targetOwnerId,
        Long userId,
        String reason,
        String description
) {
}
