package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import com.trendizm.backend.lookup.domain.vo.SourceUrl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeServiceImpl implements MemeService {

    /** 최근 등록순. 같은 시각이면 나중에 저장한 문서가 먼저 */
    private static final Sort LATEST_FIRST = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));

    private final MemeRepository memeRepository;

    @Override
    public PageResponse<MemeResponse> getMemes(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, LATEST_FIRST);
        Page<Meme> memes = (keyword == null || keyword.isBlank())
                ? memeRepository.findByStatus(MemeStatus.PUBLIC, pageable)
                : memeRepository.findByStatusAndWordContaining(MemeStatus.PUBLIC, Meme.normalizeWord(keyword), pageable);
        return PageResponse.from(memes, MemeResponse::from);
    }

    @Override
    public PageResponse<MemeResponse> getMemesByWord(String word, String sourceUrl, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, LATEST_FIRST);
        if (sourceUrl == null || sourceUrl.isBlank()) {
            return PageResponse.from(
                    memeRepository.findByWordAndStatus(Meme.normalizeWord(word), MemeStatus.PUBLIC, pageable),
                    MemeResponse::from);
        }
        // 의견 작성 때와 같은 정규화를 거쳐서, 모바일·웹 링크 어느 쪽으로 찾아도 같은 문서가 나온다
        List<Meme> found = memeRepository.findByWordAndSourceUrl(Meme.normalizeWord(word), SourceUrl.normalize(sourceUrl))
                .filter(meme -> !meme.isHidden())
                .stream().toList();
        // 결과는 0개 또는 1개라서 첫 페이지에만 담고, 목록(?q=만 보낼 때)과 같은 PageResponse 모양으로 준다
        Page<Meme> memes = new PageImpl<>(page == 0 ? found : List.of(), pageable, found.size());
        return PageResponse.from(memes, MemeResponse::from);
    }

    @Override
    public MemeResponse getMeme(Long memeId) {
        return memeRepository.findById(memeId)
                .filter(meme -> !meme.isHidden())
                .map(MemeResponse::from)
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
    }
}
