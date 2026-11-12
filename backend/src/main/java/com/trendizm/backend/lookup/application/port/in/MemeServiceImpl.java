package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.lookup.application.dto.MemeResponse;
import com.trendizm.backend.lookup.application.port.MemeService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeServiceImpl implements MemeService {

    private final MemeRepository memeRepository;

    @Override
    public Page<MemeResponse> getMemes(Pageable pageable) {
        // TODO(lookup): 밈 목록 조회
        //  1. memeRepository.findAll(pageable)
        //  2. .map(MemeResponse::from) 으로 변환해 반환
        //  (숏폼·롱폼 탭, 해시태그 조회는 컬럼이 정해지면 MemeRepository에 findByXxx를 추가)
        throw new UnsupportedOperationException("TODO(lookup): 밈 목록 조회");
    }

    @Override
    public MemeResponse getMeme(Long memeId) {
        // TODO(lookup): 밈 단건 조회
        //  1. memeRepository.findById(memeId), 없으면 예외를 던진다
        //  2. MemeResponse.from(meme) 반환
        throw new UnsupportedOperationException("TODO(lookup): 밈 단건 조회");
    }

    @Override
    @Transactional
    public Long createMeme() {
        // TODO(lookup): 밈 등록
        //  1. Meme.create(...) (영상 링크 → 임베드 URL 변환은 여기서)
        //  2. memeRepository.save(meme).getId() 반환
        throw new UnsupportedOperationException("TODO(lookup): 밈 등록");
    }

    @Override
    @Transactional
    public void updateMeme(Long memeId) {
        // TODO(lookup): 밈 수정
        //  1. memeRepository.findById(memeId), 없으면 NOT_FOUND
        //  2. Meme에 update(...) 메서드를 만들어 호출 (트랜잭션 안이라 변경 감지로 저장된다)
        throw new UnsupportedOperationException("TODO(lookup): 밈 수정");
    }

    @Override
    @Transactional
    public void deleteMeme(Long memeId) {
        // TODO(lookup): 밈 삭제
        //  1. memeRepository.findById(memeId), 없으면 NOT_FOUND
        //  2. memeRepository.delete(meme)
        throw new UnsupportedOperationException("TODO(lookup): 밈 삭제");
    }
}
