package com.trendizm.backend.auth.adapter.out.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.trendizm.backend.auth.application.port.AuthService;
import com.trendizm.backend.feed.application.port.FeedService;
import com.trendizm.backend.item.application.port.ItemService;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.notification.application.port.NotificationService;
import com.trendizm.backend.point.application.port.PointService;
import com.trendizm.backend.ranking.application.port.RankingService;
import com.trendizm.backend.rating.application.port.RatingService;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * 루트 AGENTS.md "로그인 없이 허용할 경로는 SecurityConfig의 permitAll 목록에 추가한다"를 검사한다.
 * 새 컨트롤러를 만들면 ACCESS에 공개 여부를 추가해야 테스트가 통과한다.
 */
@WebMvcTest
@Import(SecurityConfig.class)
@DisplayName("SecurityConfig 공개 경로 규칙")
class SecurityConfigTest {

    /** 컨트롤러 @RequestMapping 경로 → 로그인 없이 허용하면 true */
    private static final Map<String, Boolean> ACCESS = Map.of(
            "/api/auth", true,
            "/api/feeds", true,
            "/api/items", true,
            "/api/lookup/memes", true,
            "/api/rankings", true,
            "/api/ratings", true,
            "/api/notifications", false,
            "/api/points", false);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    @MockitoBean private AuthService authService;
    @MockitoBean private FeedService feedService;
    @MockitoBean private ItemService itemService;
    @MockitoBean private MemeService memeService;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private PointService pointService;
    @MockitoBean private RankingService rankingService;
    @MockitoBean private RatingService ratingService;

    @Test
    @DisplayName("모든 API 경로의 공개 여부가 정해져 있다")
    void 모든_경로의_공개_여부가_정해져_있다() {
        Set<String> undecided = new TreeSet<>();
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            for (String pattern : info.getPatternValues()) {
                if (pattern.equals("/error")) {
                    continue;
                }
                boolean decided = ACCESS.keySet().stream().anyMatch(base -> pattern.equals(base) || pattern.startsWith(base + "/"));
                if (!decided) {
                    undecided.add(pattern);
                }
            }
        }
        assertThat(undecided)
                .as("공개 여부가 정해지지 않은 경로. SecurityConfig와 이 테스트의 ACCESS에 추가한다")
                .isEmpty();
    }

    @Test
    @DisplayName("공개 경로는 로그인 없이 허용하고, 나머지는 로그인을 요구한다")
    void 공개_경로만_로그인_없이_허용한다() throws Exception {
        for (Map.Entry<String, Boolean> entry : ACCESS.entrySet()) {
            String path = entry.getKey() + "/1";
            int status = mockMvc.perform(get(path)).andReturn().getResponse().getStatus();
            boolean rejected = status == 401 || status == 403;
            if (entry.getValue()) {
                assertThat(rejected).as("%s는 로그인 없이 허용해야 하는데 %d 응답", path, status).isFalse();
            } else {
                assertThat(rejected).as("%s는 로그인을 요구해야 하는데 %d 응답", path, status).isTrue();
            }
        }
    }
}
