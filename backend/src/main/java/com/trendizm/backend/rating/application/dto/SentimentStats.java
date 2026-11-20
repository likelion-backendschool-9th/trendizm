package com.trendizm.backend.rating.application.dto;

/**
 * 콘텐츠 하나의 호/불호 집계.
 * likeRatio는 좋아요 비율(%)을 반올림한 값이고, 후기가 없으면 null이다.
 * 평균 별점은 소수 첫째 자리까지 반올림하고, 그쪽 후기가 없으면 null이다.
 */
public record SentimentStats(
        long likeCount,
        long dislikeCount,
        Integer likeRatio,
        Double likeAverageScore,
        Double dislikeAverageScore
) {

    public static final SentimentStats EMPTY = new SentimentStats(0, 0, null, null, null);

    public static SentimentStats of(long likeCount, long dislikeCount, Double likeAverage, Double dislikeAverage) {
        long total = likeCount + dislikeCount;
        Integer ratio = total == 0 ? null : (int) Math.round(likeCount * 100.0 / total);
        return new SentimentStats(likeCount, dislikeCount, ratio, round1(likeAverage), round1(dislikeAverage));
    }

    public long reviewCount() {
        return likeCount + dislikeCount;
    }

    private static Double round1(Double value) {
        return value == null ? null : Math.round(value * 10) / 10.0;
    }
}
