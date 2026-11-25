package com.trendizm.backend.lookup.adapter.out.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.trendizm.backend.lookup.domain.enums.ThumbnailImageType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalThumbnailStorageTest {

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};

    @TempDir
    Path dir;

    @Test
    @DisplayName("저장하면 임의 파일명(32자 + 확장자)을 주고, 그 이름으로 같은 내용과 형식을 읽는다")
    void 저장과_읽기() {
        LocalThumbnailStorage storage = new LocalThumbnailStorage(dir.resolve("thumbnails").toString());

        String filename = storage.save(PNG, ThumbnailImageType.PNG);

        assertThat(filename).matches("^[0-9a-f]{32}\\.png$");
        assertThat(storage.load(filename)).hasValueSatisfying(image -> {
            assertThat(image.content()).isEqualTo(PNG);
            assertThat(image.type()).isEqualTo(ThumbnailImageType.PNG);
        });
        assertThat(storage.save(PNG, ThumbnailImageType.PNG)).isNotEqualTo(filename);
    }

    @Test
    @DisplayName("서버가 만든 형식이 아닌 파일명(경로 이동·다른 확장자)이나 없는 파일은 읽지 않는다")
    void 잘못된_파일명() throws Exception {
        LocalThumbnailStorage storage = new LocalThumbnailStorage(dir.resolve("thumbnails").toString());
        Files.writeString(dir.resolve("secret.txt"), "secret");
        Files.createDirectories(dir.resolve("thumbnails"));
        Files.write(dir.resolve("thumbnails").resolve("0123456789abcdef0123456789abcdef.svg"), PNG);

        for (String filename : List.of("../secret.txt", "..%2Fsecret.txt", "secret.txt", "a.jpg",
                "0123456789abcdef0123456789abcdef.svg", "0123456789abcdef0123456789abcdef.jpg", "")) {
            assertThat(storage.load(filename)).as(filename).isEmpty();
        }
        assertThat(storage.load(null)).isEmpty();
    }
}
