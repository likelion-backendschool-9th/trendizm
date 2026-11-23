package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.global.response.PageResponse;
import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import com.trendizm.backend.lookup.domain.enums.MemeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public MemeResponse getMeme(String word) {
        return memeRepository.findByWordAndStatus(Meme.normalizeWord(word), MemeStatus.PUBLIC)
                .map(MemeResponse::from)
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
    }
}
