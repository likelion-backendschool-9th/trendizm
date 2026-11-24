package com.trendizm.backend.lookup.adapter.out.storage;

import com.trendizm.backend.lookup.application.port.out.DownloadedImage;
import com.trendizm.backend.lookup.application.port.out.ThumbnailStorage;
import com.trendizm.backend.lookup.domain.enums.ThumbnailImageType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 썸네일을 서버 디스크의 한 폴더에 저장한다. 폴더는 trendizm.thumbnail.dir (환경 변수 TRENDIZM_THUMBNAIL_DIR), 기본값 data/thumbnails.
 * 운영(Docker)에서는 이 폴더를 볼륨으로 잡아야 컨테이너를 다시 만들어도 파일이 남는다.
 * 파일명은 서버가 만든 임의 이름(UUID 32자) + 확장자뿐이다. 읽을 때도 이 형식만 받아서 ../ 같은 경로로 다른 파일을 읽지 못하게 한다.
 */
@Component
public class LocalThumbnailStorage implements ThumbnailStorage {

    private static final Pattern FILENAME = Pattern.compile("^[0-9a-f]{32}\\.([a-z]+)$");

    private final Path directory;

    public LocalThumbnailStorage(@Value("${trendizm.thumbnail.dir:data/thumbnails}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public String save(byte[] content, ThumbnailImageType type) {
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + type.extension();
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve(filename), content, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            throw new UncheckedIOException("썸네일 저장 실패: " + filename, e);
        }
        return filename;
    }

    @Override
    public Optional<DownloadedImage> load(String filename) {
        if (filename == null) {
            return Optional.empty();
        }
        Matcher matcher = FILENAME.matcher(filename);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        Optional<ThumbnailImageType> type = ThumbnailImageType.fromExtension(matcher.group(1));
        Path file = directory.resolve(filename);
        if (type.isEmpty() || !Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(new DownloadedImage(Files.readAllBytes(file), type.get()));
        } catch (IOException e) {
            throw new UncheckedIOException("썸네일 읽기 실패: " + filename, e);
        }
    }
}
