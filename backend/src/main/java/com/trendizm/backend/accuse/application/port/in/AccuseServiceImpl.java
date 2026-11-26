package com.trendizm.backend.accuse.application.port.in;

import com.trendizm.backend.accuse.application.dto.AccuseResponse;
import com.trendizm.backend.accuse.application.dto.CreateAccuseCommand;
import com.trendizm.backend.accuse.application.exception.AccuseErrorCode;
import com.trendizm.backend.accuse.application.port.AccuseService;
import com.trendizm.backend.accuse.application.port.out.AccuseRepository;
import com.trendizm.backend.accuse.domain.entity.Accuse;
import com.trendizm.backend.accuse.domain.enums.AccuseReason;
import com.trendizm.backend.accuse.domain.enums.AccuseTarget;
import com.trendizm.backend.global.error.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccuseServiceImpl implements AccuseService {

    private final AccuseRepository accuseRepository;

    /**
     * 이미 신고했는지 먼저 확인해 대부분은 여기서 409로 끝낸다.
     * 확인과 저장 사이에 같은 사람이 한 번 더 보내면 DB의 (target, target_id, user_id) UNIQUE에서 막히고, 그것도 409로 바꾼다
     */
    @Override
    @Transactional
    public AccuseResponse accuse(CreateAccuseCommand command) {
        AccuseTarget target = AccuseTarget.fromSlug(command.target())
                .orElseThrow(() -> new IllegalArgumentException("없는 신고 대상 종류입니다: " + command.target()));
        AccuseReason reason = AccuseReason.fromSlug(command.reason())
                .orElseThrow(() -> new BusinessException(AccuseErrorCode.ACCUSE_INVALID_REASON));
        String description = Accuse.normalizeDescription(command.description());
        if (reason.requiresDescription() && description == null) {
            throw new BusinessException(AccuseErrorCode.ACCUSE_DESCRIPTION_REQUIRED);
        }
        if (description != null && description.length() > Accuse.MAX_DESCRIPTION_LENGTH) {
            throw new BusinessException(AccuseErrorCode.ACCUSE_DESCRIPTION_TOO_LONG);
        }
        if (accuseRepository.existsByTargetAndTargetIdAndUserId(target, command.targetId(), command.userId())) {
            throw new BusinessException(AccuseErrorCode.ACCUSE_ALREADY_EXISTS);
        }
        try {
            Accuse saved = accuseRepository.saveAndFlush(
                    Accuse.create(target, command.targetId(), command.userId(), reason, description));
            return AccuseResponse.from(saved);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(AccuseErrorCode.ACCUSE_ALREADY_EXISTS);
        }
    }
}
