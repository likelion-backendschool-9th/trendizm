package com.trendizm.backend.ranking.application.port;

import com.trendizm.backend.ranking.application.dto.RankingImportResult;
import com.trendizm.backend.ranking.application.dto.RankingImportRow;
import com.trendizm.backend.ranking.application.dto.RankingSnapshot;
import com.trendizm.backend.ranking.application.dto.RankingTabs;
import java.util.List;

public interface RankingService {

    /**
     * 플랫폼·카테고리의 가장 최근 스냅샷(1~10위). platform과 category는 slug다(예: youtube, challenge).
     * 없는 플랫폼·카테고리(미분류 포함)면 예외를 던지고, 아직 수집하지 않았으면 빈 스냅샷을 준다.
     */
    RankingSnapshot getLatestSnapshot(String platform, String category);

    /** 순위 화면의 탭. 플랫폼 전체(enum 선언 순서)와 미분류를 뺀 카테고리 전체(id 순) */
    RankingTabs getTabs();

    /**
     * 순위 CSV(관리자 업로드)를 저장한다. 틀린 줄이 있거나 같은 수집 결과를 이미 올렸으면 아무것도 저장하지 않고 예외를 던진다.
     * DB에 없는 카테고리와 순위가 빈 줄은 건너뛰고 결과의 skipped로 알린다.
     */
    RankingImportResult importRankings(List<RankingImportRow> rows);
}
