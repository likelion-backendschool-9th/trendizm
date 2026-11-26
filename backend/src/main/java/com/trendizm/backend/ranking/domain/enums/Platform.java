package com.trendizm.backend.ranking.domain.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 트렌드 순위 플랫폼.
 * 상수 이름이 그대로 slug다. 화면 주소(frontend/app/ranking/[platform]/[category])의 platform 자리에 쓰고,
 * @Enumerated(EnumType.STRING)으로 저장하면 DB에도 이 소문자 이름으로 들어간다.
 * 괄호 안은 화면 탭에 보여 줄 이름(label)과 관련 게시물 주소의 틀이다.
 * 주소 틀의 {id}에는 게시물 ID(shortcode), {author}에는 작성자 계정 이름이 들어간다.
 * 선언 순서가 화면의 플랫폼 탭 순서다(GET /api/rankings/tabs).
 */
public enum Platform {
    youtube("YouTube", "https://www.youtube.com/watch?v={id}"),
    instagram("Instagram", "https://www.instagram.com/p/{id}/"),
    threads("Threads", "https://www.threads.com/@{author}/post/{id}"),
    x("X", "https://x.com/i/status/{id}"),
    tiktok("TikTok", "https://www.tiktok.com/@{author}/video/{id}");

    private static final Pattern POST_ID = Pattern.compile("[A-Za-z0-9_-]+");
    private static final Pattern AUTHOR = Pattern.compile("[A-Za-z0-9._]+");

    /** 화면에 보여 줄 이름. name()은 slug(소문자 상수 이름)라서 따로 둔다 */
    private final String label;
    private final String postUrlTemplate;

    Platform(String label, String postUrlTemplate) {
        this.label = label;
        this.postUrlTemplate = postUrlTemplate;
    }

    public String getLabel() {
        return label;
    }

    /** slug로 찾는다. 예: "youtube" → youtube. 없으면 빈 Optional (valueOf는 없으면 예외를 던진다) */
    public static Optional<Platform> fromSlug(String slug) {
        return Arrays.stream(values()).filter(value -> value.name().equals(slug)).findFirst();
    }

    /** 게시물 ID는 영문·숫자·_·-만 쓸 수 있다. 다른 문자로 주소가 만들어지지 않게 한다 */
    public static boolean isValidPostId(String postId) {
        return postId != null && POST_ID.matcher(postId).matches();
    }

    /** 작성자 계정 이름은 영문·숫자·.·_만 쓸 수 있다 */
    public static boolean isValidAuthor(String author) {
        return author != null && AUTHOR.matcher(author).matches();
    }

    /** 주소에 작성자가 들어가는 플랫폼이면 true (threads, tiktok) */
    public boolean requiresAuthor() {
        return postUrlTemplate.contains("{author}");
    }

    /**
     * 관련 게시물 주소. 예: instagram.postUrl("Da0E9_oBZM-", null) → https://www.instagram.com/p/Da0E9_oBZM-/
     * 작성자가 필요 없는 플랫폼은 author를 쓰지 않는다. 형식에 맞지 않으면 IllegalArgumentException
     */
    public String postUrl(String postId, String author) {
        if (!isValidPostId(postId)) {
            throw new IllegalArgumentException("게시물 ID 형식이 아닙니다. postId=" + postId);
        }
        String url = postUrlTemplate.replace("{id}", postId);
        if (!requiresAuthor()) {
            return url;
        }
        if (!isValidAuthor(author)) {
            throw new IllegalArgumentException(name() + " 주소에는 작성자 계정 이름이 필요합니다. author=" + author);
        }
        return url.replace("{author}", author);
    }
}
