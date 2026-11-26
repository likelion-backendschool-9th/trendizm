package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.dto.CreateAccuseCommand;
import com.trendizm.backend.accuse.application.port.AccuseService;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.CreateMemeAccuseCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeAccuseService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeAccuseServiceImpl implements MemeAccuseService {

    /** accuse 도메인의 신고 대상 종류 slug (AccuseTarget.MEME) */
    static final String TARGET_MEME = "meme";

    private final MemeRepository memeRepository;
    /** 호출 방향은 lookup → accuse만이다. accuse는 lookup을 부르지 않는다 */
    private final AccuseService accuseService;

    @Override
    @Transactional
    public AccuseResponse accuseMeme(CreateMemeAccuseCommand command) {
        Long memeId = memeRepository.findById(command.memeId())
                .filter(meme -> !meme.isHidden())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND))
                .getId();
        return accuseService.accuse(new CreateAccuseCommand(
                TARGET_MEME, memeId, command.userId(), command.reason(), command.description()));
    }
}
