package com.newstrust.domain.port.in;

import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.model.SourceSubmission;

public interface AnalyzeSourceUseCase {

    SourceAnalysis analyze(SourceSubmission submission);
}
