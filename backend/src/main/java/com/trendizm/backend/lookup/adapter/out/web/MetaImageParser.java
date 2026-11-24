package com.trendizm.backend.lookup.adapter.out.web;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML에서 대표 이미지 메타 태그를 찾는다. 링크 미리보기(카카오톡·슬랙 등)가 쓰는 것과 같은 태그다.
 * 우선순위: og:image:secure_url → og:image → twitter:image → twitter:image:src. 같은 이름이 여러 개면 처음 것.
 * 태그 몇 개만 읽으면 되므로 HTML 파서 의존성을 추가하지 않고 정규식으로 찾는다.
 */
final class MetaImageParser {

    private static final List<String> KEYS = List.of("og:image:secure_url", "og:image", "twitter:image", "twitter:image:src");
    private static final Pattern META_TAG = Pattern.compile("<meta\\s[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ATTRIBUTE =
            Pattern.compile("([a-zA-Z_:-]+)\\s*=\\s*(?:\"([^\"]*)\"|'([^']*)'|([^\\s\"'>]+))");
    private static final Pattern NUMERIC_ENTITY = Pattern.compile("&#(x[0-9a-fA-F]+|[0-9]+);");

    private MetaImageParser() {
    }

    /** 찾은 이미지 주소. 상대 주소는 페이지 주소(base) 기준으로 풀고, http·https 주소만 준다 */
    static Optional<String> find(String html, URI base) {
        Map<String, String> found = new HashMap<>();
        Matcher tag = META_TAG.matcher(html);
        while (tag.find()) {
            Map<String, String> attributes = attributes(tag.group());
            String key = attributes.getOrDefault("property", attributes.get("name"));
            String content = attributes.get("content");
            if (key != null && content != null && !content.isBlank()) {
                found.putIfAbsent(key.toLowerCase(Locale.ROOT), content.strip());
            }
        }
        for (String key : KEYS) {
            Optional<String> url = Optional.ofNullable(found.get(key)).flatMap(value -> resolve(base, decode(value)));
            if (url.isPresent()) {
                return url;
            }
        }
        return Optional.empty();
    }

    private static Map<String, String> attributes(String tag) {
        Map<String, String> attributes = new HashMap<>();
        Matcher matcher = ATTRIBUTE.matcher(tag);
        while (matcher.find()) {
            String value = matcher.group(2) != null ? matcher.group(2)
                    : matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            attributes.putIfAbsent(matcher.group(1).toLowerCase(Locale.ROOT), value);
        }
        return attributes;
    }

    /** 주소에 자주 나오는 HTML 엔티티만 푼다 (&amp;amp; 등) */
    static String decode(String value) {
        Matcher matcher = NUMERIC_ENTITY.matcher(value);
        StringBuilder decoded = new StringBuilder();
        while (matcher.find()) {
            String number = matcher.group(1);
            int codePoint = number.startsWith("x") ? Integer.parseInt(number.substring(1), 16) : Integer.parseInt(number);
            matcher.appendReplacement(decoded, Matcher.quoteReplacement(new String(Character.toChars(codePoint))));
        }
        matcher.appendTail(decoded);
        return decoded.toString()
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&");
    }

    private static Optional<String> resolve(URI base, String value) {
        try {
            URI resolved = base.resolve(value.replace(" ", "%20"));
            String scheme = resolved.getScheme() == null ? "" : resolved.getScheme().toLowerCase(Locale.ROOT);
            return scheme.equals("http") || scheme.equals("https") ? Optional.of(resolved.toString()) : Optional.empty();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
