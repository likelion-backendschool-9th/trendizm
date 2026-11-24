package com.trendizm.backend.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 스프링 전체를 띄우지 않고, 테스트용 컨트롤러로 예외가 { code, message }로 바뀌는지 확인한다 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("BusinessException은 ErrorCode의 상태와 코드로 응답한다")
    void 비즈니스_예외() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("테스트 대상을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("BusinessException에 메시지를 넘기면 그 메시지로 응답한다")
    void 비즈니스_예외_메시지_지정() throws Exception {
        mockMvc.perform(get("/test/business-message"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("3번 대상이 없습니다."));
    }

    @Test
    @DisplayName("BusinessException에 항목별 오류를 넘기면 errors에 담아 응답하고, 없으면 errors를 빼고 응답한다")
    void 비즈니스_예외_항목별_오류() throws Exception {
        mockMvc.perform(get("/test/business-errors"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TEST_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("테스트 대상을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.errors[0].field").value("3번째 줄"))
                .andExpect(jsonPath("$.errors[0].message").value("값이 없습니다."));
        mockMvc.perform(get("/test/business"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("@Valid 검증에 실패하면 400 INVALID_INPUT과 필드별 오류 목록을 준다")
    void 검증_실패() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"email\": \"abc\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value("입력값이 올바르지 않습니다."))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.field == 'name')].message").value("이름은 필수입니다."))
                .andExpect(jsonPath("$.errors[?(@.field == 'email')].message").value("이메일 형식이 아닙니다."));
    }

    @Test
    @DisplayName("JSON 형식이 잘못되면 400 INVALID_INPUT으로 응답하고 errors는 없다")
    void 잘못된_JSON() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{잘못된 json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("필수 파라미터가 없으면 400 INVALID_INPUT으로 응답한다")
    void 필수_파라미터_누락() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("파라미터 검증에 실패하면 파라미터 이름을 field로 알려 준다")
    void 파라미터_검증_실패() throws Exception {
        mockMvc.perform(get("/test/size").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.errors[0].field").value("size"))
                .andExpect(jsonPath("$.errors[0].message").value("size는 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("JSON이 아닌 형식으로 보내면 415 UNSUPPORTED_MEDIA_TYPE으로 응답한다")
    void 지원하지_않는_형식() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("name=abc"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    @DisplayName("따로 처리하지 않은 스프링 요청 오류는 500이 아니라 스프링이 정한 4xx로 응답한다")
    void 그_외_요청_오류() throws Exception {
        mockMvc.perform(get("/test/header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 405 METHOD_NOT_ALLOWED로 응답한다")
    void 지원하지_않는_메서드() throws Exception {
        mockMvc.perform(post("/test/business"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("컨트롤러에서 난 권한 예외는 403 FORBIDDEN으로 응답한다")
    void 권한_없음() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500 INTERNAL_ERROR로 응답하고 내부 메시지를 노출하지 않는다")
    void 예상하지_못한_예외() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("서버 오류가 발생했습니다."));
    }

    enum TestErrorCode implements ErrorCode {
        TEST_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트 대상을 찾을 수 없습니다.");

        private final HttpStatus status;
        private final String message;

        TestErrorCode(HttpStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        @Override
        public HttpStatus status() {
            return status;
        }

        @Override
        public String code() {
            return name();
        }

        @Override
        public String message() {
            return message;
        }
    }

    record TestRequest(
            @NotBlank(message = "이름은 필수입니다.") String name,
            @Email(message = "이메일 형식이 아닙니다.") String email
    ) {
    }

    @RestController
    static class TestController {

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(TestErrorCode.TEST_NOT_FOUND);
        }

        @GetMapping("/test/business-message")
        void businessWithMessage() {
            throw new BusinessException(TestErrorCode.TEST_NOT_FOUND, "3번 대상이 없습니다.");
        }

        @GetMapping("/test/business-errors")
        void businessWithErrors() {
            throw new BusinessException(TestErrorCode.TEST_NOT_FOUND,
                    java.util.List.of(new ErrorResponse.FieldError("3번째 줄", "값이 없습니다.")));
        }

        @PostMapping("/test/valid")
        void valid(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/param")
        void param(@RequestParam String keyword) {
        }

        @GetMapping("/test/size")
        void size(@RequestParam @Min(value = 1, message = "size는 1 이상이어야 합니다.") int size) {
        }

        @GetMapping("/test/header")
        void header(@RequestHeader("X-Test") String value) {
        }

        @GetMapping("/test/forbidden")
        void forbidden() {
            throw new AccessDeniedException("관리자만 가능");
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("DB 비밀번호가 틀렸습니다");
        }
    }
}
