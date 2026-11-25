package com.trendizm.backend.lookup.domain.enums;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * 썸네일로 받는 이미지 형식. 이 밖의 형식(SVG 등)은 받지 않는다.
 * SVG는 스크립트를 담을 수 있어서, 우리 주소로 내려주면 XSS가 될 수 있다.
 */
public enum ThumbnailImageType {

    JPG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp"),
    GIF("gif", "image/gif");

    private final String extension;
    private final String contentType;

    ThumbnailImageType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    /** Content-Type 헤더(예: "image/jpeg; charset=...")로 찾는다 */
    public static Optional<ThumbnailImageType> fromContentType(String header) {
        if (header == null) {
            return Optional.empty();
        }
        String mediaType = header.split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
        return Arrays.stream(values()).filter(type -> type.contentType.equals(mediaType)).findFirst();
    }

    public static Optional<ThumbnailImageType> fromExtension(String extension) {
        return Arrays.stream(values()).filter(type -> type.extension.equals(extension)).findFirst();
    }

    /** 파일 앞부분(매직 넘버)이 이 형식과 맞는지. Content-Type만 믿고 HTML 같은 다른 파일을 이미지로 저장하지 않게 한다 */
    public boolean matches(byte[] content) {
        return switch (this) {
            case JPG -> startsWith(content, 0, 0xFF, 0xD8, 0xFF);
            case PNG -> startsWith(content, 0, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
            case GIF -> startsWith(content, 0, 'G', 'I', 'F', '8');
            case WEBP -> startsWith(content, 0, 'R', 'I', 'F', 'F') && startsWith(content, 8, 'W', 'E', 'B', 'P');
        };
    }

    private static boolean startsWith(byte[] content, int offset, int... expected) {
        if (content.length < offset + expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if ((content[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }
}
