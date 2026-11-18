package com.trendizm.backend.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.member.application.port.out.MemberRepository;
import com.trendizm.backend.member.domain.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 이미 가입한 회원 1명, 탈퇴한 회원 1명. @Transactional이라 테스트가 끝나면 롤백된다 */
    @BeforeEach
    void setUp() {
        memberRepository.save(Member.create("member@test.com", "encoded-password", "테스트회원", false));
        Member withdrawn = Member.create("withdrawn@test.com", "encoded-password", "탈퇴회원", false);
        withdrawn.withdraw();
        memberRepository.save(withdrawn);
    }

    private ResultActions signup(String email, String password, String nickname, boolean agreeTerms)
            throws Exception {
        String body = """
                {"email": "%s", "password": "%s", "nickname": "%s",
                 "agreeTerms": %s, "agreePrivacy": true, "marketingAgreed": true}
                """.formatted(email, password, nickname, agreeTerms);
        return mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    @DisplayName("회원가입에 성공하면 201이고, 이메일은 소문자로·비밀번호는 암호화해서 저장한다")
    void 회원가입_성공() throws Exception {
        signup("New@Test.com", "password123", "새회원", true)
                .andExpect(status().isCreated());

        Member saved = memberRepository.findByEmail("new@test.com").orElseThrow();
        assertThat(saved.getNickname()).isEqualTo("새회원");
        assertThat(saved.isMarketingAgreed()).isTrue();
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("형식이 틀리면 400 INVALID_INPUT과 틀린 필드를 모두 알려 준다")
    void 형식_오류() throws Exception {
        signup("not-email", "short", "공 백", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'nickname')]").exists());
    }

    @Test
    @DisplayName("필수 약관에 동의하지 않으면 400")
    void 필수_약관_미동의() throws Exception {
        signup("new@test.com", "password123", "새회원", false)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("agreeTerms"));
    }

    @Test
    @DisplayName("이미 있는 이메일이면 대소문자가 달라도 409 DUPLICATE_EMAIL")
    void 이메일_중복() throws Exception {
        signup("MEMBER@Test.com", "password123", "새회원", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("탈퇴 후 익명화 전인 이메일도 409 DUPLICATE_EMAIL")
    void 탈퇴_이메일_중복() throws Exception {
        signup("withdrawn@test.com", "password123", "새회원", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("이미 있는 닉네임이면 409 DUPLICATE_NICKNAME")
    void 닉네임_중복() throws Exception {
        signup("new@test.com", "password123", "테스트회원", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_NICKNAME"));
    }

    @Test
    @DisplayName("이메일 중복 확인: 없으면 true, 있으면(대소문자 무시) false")
    void 이메일_중복_확인() throws Exception {
        mockMvc.perform(get("/api/auth/check-email").param("email", "new@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));
        mockMvc.perform(get("/api/auth/check-email").param("email", "Member@Test.com"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("이메일 중복 확인에 이메일 형식이 아니면 400")
    void 이메일_중복_확인_형식_오류() throws Exception {
        mockMvc.perform(get("/api/auth/check-email").param("email", "not-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    @DisplayName("닉네임 중복 확인: 없으면 true, 있으면 false")
    void 닉네임_중복_확인() throws Exception {
        mockMvc.perform(get("/api/auth/check-nickname").param("nickname", "새회원"))
                .andExpect(jsonPath("$.available").value(true));
        mockMvc.perform(get("/api/auth/check-nickname").param("nickname", "테스트회원"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    @DisplayName("JSON이 아닌 형식으로 가입 요청하면 415 (로그인 없이도 보안 필터를 통과해 같은 형식으로 응답)")
    void 지원하지_않는_형식() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("email=a@test.com"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }
}
