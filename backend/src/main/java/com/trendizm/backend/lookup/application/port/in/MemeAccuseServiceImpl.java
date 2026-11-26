package com.trendizm.backend.lookup.application.port.in;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.dto.CreateAccuseCommand;
import com.trendizm.backend.accuse.application.port.AccuseService;
import com.trendizm.backend.global.error.BusinessException;
import com.trendizm.backend.lookup.application.dto.CreateMemeAccuseCommand;
import com.trendizm.backend.lookup.application.exception.MemeErrorCode;
import com.trendizm.backend.lookup.application.port.MemeAccuseService;
import com.trendizm.backend.lookup.application.port.out.MemeRepository;
import com.trendizm.backend.lookup.domain.entity.Meme;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemeAccuseServiceImpl implements MemeAccuseService {

    private final MemeRepository memeRepository;
    /** 호출 방향은 lookup → accuse만이다. accuse는 lookup을 부르지 않는다 */
    private final AccuseService accuseService;

    @Override
    @Transactional
    public AccuseResponse accuseMeme(CreateMemeAccuseCommand command) {
        Meme meme = memeRepository.findById(command.memeId())
                .filter(found -> !found.isHidden())
                .orElseThrow(() -> new BusinessException(MemeErrorCode.MEME_NOT_FOUND));
        return accuseService.accuse(new CreateAccuseCommand(Meme.TABLE_NAME, meme.getId(), meme.getUserId(),
                command.userId(), command.reason(), command.description()));
    }
}
