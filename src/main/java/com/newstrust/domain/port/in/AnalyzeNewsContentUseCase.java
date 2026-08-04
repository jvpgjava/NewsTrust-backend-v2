package com.newstrust.domain.port.in;

import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.NewsContentSubmission;

public interface AnalyzeNewsContentUseCase {

    NewsAnalysis analyze(NewsContentSubmission submission);
}
