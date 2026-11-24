package com.trendizm.backend.lookup.adapter.in.web;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.trendizm.backend.auth.adapter.out.security.SecurityConfig;
import com.trendizm.backend.lookup.application.dto.MemeThumbnailImage;
import com.trendizm.backend.lookup.application.port.MemeThumbnailService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MemeThumbnailController.class)
@Import(SecurityConfig.class)
class MemeThumbnailControllerTest {

    private static final String THUMBNAIL = "/api/lookup/memes/thumbnails/{filename}";
    private static final String FILENAME = "0123456789abcdef0123456789abcdef.png";
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemeThumbnailService thumbnailService;

    @Test
    @DisplayName("로그인 없이 이미지를 받는다. 저장한 형식의 Content-Type, 긴 캐시, nosniff를 붙인다")
    void 이미지() throws Exception {
        given(thumbnailService.getThumbnail(FILENAME)).willReturn(Optional.of(new MemeThumbnailImage(PNG, "image/png")));

        mockMvc.perform(get(THUMBNAIL, FILENAME))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"))
                .andExpect(header().string("Cache-Control", "max-age=2592000, public, immutable"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(content().bytes(PNG));
    }

    @Test
    @DisplayName("없는 파일이면 404")
    void 없음() throws Exception {
        given(thumbnailService.getThumbnail("0123456789abcdef0123456789abcdef.jpg")).willReturn(Optional.empty());

        mockMvc.perform(get(THUMBNAIL, "0123456789abcdef0123456789abcdef.jpg"))
                .andExpect(status().isNotFound());
    }
}
