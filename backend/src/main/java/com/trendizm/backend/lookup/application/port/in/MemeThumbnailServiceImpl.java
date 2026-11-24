package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.lookup.application.dto.MemeCreatedEvent;
import com.trendizm.backend.lookup.application.dto.MemeThumbnailImage;
import com.trendizm.backend.lookup.application.port.MemeThumbnailService;
import com.trendizm.backend.lookup.application.port.out.DownloadedImage;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.application.port.out.ThumbnailStorage;
import com.trendizm.backend.lookup.application.port.out.WebImageClient;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.vo.SourceUrl;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 썸네일 채우기는 외부 접속(최대 수 초)이 있어서 문서 작성 요청과 분리한다.
 * - 문서 작성 트랜잭션이 커밋된 뒤에(AFTER_COMMIT) 다른 스레드에서(@Async) 실행한다. 작성 응답이 느려지지 않고,
 *   외부 접속 중에 DB 연결을 잡고 있지 않으며, 실패해도 문서 작성에는 영향이 없다.
 * - 외부 접속은 트랜잭션 밖에서 하고(NOT_SUPPORTED), 파일명 기록만 짧은 트랜잭션으로 한다.
 * - trendizm.thumbnail.fetch-enabled=false면 문서가 만들어져도 채우지 않는다. 테스트에서 실제 외부 접속을 막는 데 쓴다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeThumbnailServiceImpl implements MemeThumbnailService {

    private final MemeRepository memeRepository;
    private final WebImageClient webImageClient;
    private final ThumbnailStorage thumbnailStorage;
    private final TransactionTemplate transactionTemplate;

    @Value("${trendizm.thumbnail.fetch-enabled:true}")
    private boolean fetchEnabled;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void onMemeCreated(MemeCreatedEvent event) {
        if (!fetchEnabled) {
            return;
        }
        fillThumbnail(event.memeId());
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void fillThumbnail(Long memeId) {
        try {
            Optional<Meme> meme = memeRepository.findById(memeId).filter(found -> found.getThumbnail() == null);
            if (meme.isEmpty()) {
                return;
            }
            String sourceUrl = meme.get().getSourceUrl();
            Optional<DownloadedImage> image = SourceUrl.youtubeThumbnail(sourceUrl)
                    .or(() -> webImageClient.findImageUrl(sourceUrl))
                    .flatMap(webImageClient::download);
            if (image.isEmpty()) {
                log.info("썸네일 없음 memeId={} source={}", memeId, sourceUrl);
                return;
            }
            String filename = thumbnailStorage.save(image.get().content(), image.get().type());
            transactionTemplate.executeWithoutResult(status ->
                    memeRepository.findById(memeId).ifPresent(found -> found.changeThumbnail(filename)));
        } catch (RuntimeException e) {
            // 비동기라 호출한 쪽에 전달되지 않는다. 썸네일은 없어도 되므로 기록만 한다
            log.warn("썸네일 저장 실패 memeId={}", memeId, e);
        }
    }

    /** 파일만 읽으므로 DB 트랜잭션을 열지 않는다 */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Optional<MemeThumbnailImage> getThumbnail(String filename) {
        return thumbnailStorage.load(filename)
                .map(image -> new MemeThumbnailImage(image.content(), image.type().contentType()));
    }
}
