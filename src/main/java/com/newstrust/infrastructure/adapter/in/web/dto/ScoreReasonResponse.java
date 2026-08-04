package com.newstrust.infrastructure.adapter.in.web.dto;

import com.newstrust.domain.model.ScoreReason;

public record ScoreReasonResponse(String factor, String description) {

    public static ScoreReasonResponse from(ScoreReason reason) {
        return new ScoreReasonResponse(reason.factor().name(), reason.description());
    }
}
