package com.trendizm.backend.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.trendizm.backend.auth.adapter.out.security.CustomUserDetails;
import com.trendizm.backend.auth.application.port.out.TokenProvider;
import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.Member;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(AuthLoginTest.SecurityTestController.class)
class AuthLoginTest {

    /**
     * 테스트 전용 API. 로그인이 필요한 경로와 관리자 전용 경로가 아직 없어서 보안 설정 확인용으로 만든다.
     * 테스트 클래스 안에 두면 다른 테스트(SecurityConfigTest 등)에서는 스캔되지 않는다.
     */
    @RestController
    static class SecurityTestController {

        /** 로그인 필요 (SecurityConfig의 anyRequest). AGENTS.md 규칙대로 id만 받는다 */
        @GetMapping("/api/test/me")
        Map<String, Object> me(@AuthenticationPrincipal(expression = "id") Long userId,
                @AuthenticationPrincipal CustomUserDetails user) {
            return Map.of("id", userId, "role", user.getAuthorities().iterator().next().getAuthority());
        }

        /** ADMIN만 (SecurityConfig의 /api/admin/**) */
        @GetMapping("/api/admin/test")
        String admin() {
            return "ok";
        }
    }

    private static final String PASSWORD = "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 로그인할 회원 1명, 탈퇴한 회원 1명. 비밀번호는 둘 다 PASSWORD. @Transactional이라 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        String encoded = passwordEncoder.encode(PASSWORD);
        memberRepository.save(Member.create("login@test.com", encoded, "로그인회원", false));
        Member withdrawn = Member.create("withdrawn@test.com", encoded, "탈퇴회원", false);
        withdrawn.withdraw();
        memberRepository.save(withdrawn);
    }

    private ResultActions login(String email, String password, boolean rememberMe) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s", "rememberMe": %s}
                        """.formatted(email, password, rememberMe)));
    }

    private ResultActions reissue(String refreshToken) throws Exception {
        return mockMvc.perform(post("/api/auth/reissue")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"refreshToken": "%s"}
                        """.formatted(refreshToken)));
    }

    private static String read(ResultActions result, String path) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), path);
    }

    private static String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    @Test
    @DisplayName("로그인하면 회원 정보와 두 토큰, 만료 시간(초)을 본문으로 주고 쿠키는 내려 주지 않는다")
    void 로그인_성공() throws Exception {
        ResultActions result = login("login@test.com", PASSWORD, false)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.nickname").value("로그인회원"))
                .andExpect(jsonPath("$.member.role").value("USER"))
                .andExpect(jsonPath("$.accessTokenExpiresIn").value(30 * 60))
                .andExpect(jsonPath("$.refreshTokenExpiresIn").value(14 * 24 * 60 * 60))
                .andExpect(jsonPath("$.rememberMe").value(false))
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        assertThat(tokenProvider.parseAccessToken(read(result, "$.accessToken"))).isPresent();
        assertThat(read(result, "$.refreshToken")).isNotBlank();
    }

    @Test
    @DisplayName("로그인 상태 유지를 체크하면 rememberMe가 true로 온다")
    void 로그인_상태_유지() throws Exception {
        login("login@test.com", PASSWORD, true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rememberMe").value(true));
    }

    @Test
    @DisplayName("이메일은 대소문자를 구분하지 않는다")
    void 대소문자_무시() throws Exception {
        login("LOGIN@Test.com", PASSWORD, false).andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호가 틀려도, 없는 이메일이어도, 탈퇴 회원이어도 같은 401 LOGIN_FAILED")
    void 로그인_실패() throws Exception {
        login("login@test.com", "wrong-password", false)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
        login("none@test.com", PASSWORD, false)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
        login("withdrawn@test.com", PASSWORD, false)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("LOGIN_FAILED"));
    }

    @Test
    @DisplayName("가입하면 바로 로그인 토큰을 받는다(로그인 상태 유지는 꺼짐)")
    void 가입_후_자동_로그인() throws Exception {
        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "new@test.com", "password": "password123", "nickname": "새회원",
                                 "agreeTerms": true, "agreePrivacy": true, "marketingAgreed": false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.member.nickname").value("새회원"))
                .andExpect(jsonPath("$.rememberMe").value(false));

        assertThat(tokenProvider.parseAccessToken(read(result, "$.accessToken"))).isPresent();
        assertThat(read(result, "$.refreshToken")).isNotBlank();
    }

    @Test
    @DisplayName("로그인이 필요한 경로: 헤더가 없으면 401 UNAUTHORIZED, Bearer 토큰이 있으면 로그인한 회원으로 처리")
    void 인증_필요_경로() throws Exception {
        mockMvc.perform(get("/api/test/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다."));

        String accessToken = tokenProvider.createAccessToken(42L, "USER");
        mockMvc.perform(get("/api/test/me").header(HttpHeaders.AUTHORIZATION, bearer(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
        // "bearer"를 소문자로 써도 받는다
        mockMvc.perform(get("/api/test/me").header(HttpHeaders.AUTHORIZATION, "bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("위조된 토큰이거나 Bearer 형식이 아니면 로그인하지 않은 것으로 보고 401")
    void 위조_토큰() throws Exception {
        mockMvc.perform(get("/api/test/me").header(HttpHeaders.AUTHORIZATION, bearer("forged.token.value")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/test/me")
                        .header(HttpHeaders.AUTHORIZATION, tokenProvider.createAccessToken(1L, "USER")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("관리자 경로: USER면 403 FORBIDDEN, ADMIN이면 통과")
    void 관리자_경로() throws Exception {
        mockMvc.perform(get("/api/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenProvider.createAccessToken(1L, "USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(get("/api/admin/test")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenProvider.createAccessToken(1L, "ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("재발급: 새 토큰을 받고 로그인 상태 유지가 이어지며, 이미 쓴 리프레시 토큰은 다시 쓸 수 없다")
    void 재발급() throws Exception {
        String refresh = read(login("login@test.com", PASSWORD, true), "$.refreshToken");

        ResultActions reissued = reissue(refresh)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.member.nickname").value("로그인회원"))
                .andExpect(jsonPath("$.rememberMe").value(true));
        assertThat(read(reissued, "$.refreshToken")).isNotEqualTo(refresh);

        reissue(refresh)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    @DisplayName("리프레시 토큰 없이 재발급하면 401 INVALID_REFRESH_TOKEN")
    void 재발급_토큰_없음() throws Exception {
        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
        mockMvc.perform(post("/api/auth/reissue").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }
}
