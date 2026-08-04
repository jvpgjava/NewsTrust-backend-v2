package com.newstrust.infrastructure.adapter.in.web;

import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.NewsContentSubmission;
import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.model.SourceSubmission;
import com.newstrust.domain.port.in.AnalyzeNewsContentUseCase;
import com.newstrust.domain.port.in.AnalyzeSourceUseCase;
import com.newstrust.infrastructure.adapter.in.web.dto.NewsContentAnalysisRequest;
import com.newstrust.infrastructure.adapter.in.web.dto.NewsContentAnalysisResponse;
import com.newstrust.infrastructure.adapter.in.web.dto.SourceAnalysisRequest;
import com.newstrust.infrastructure.adapter.in.web.dto.SourceAnalysisResponse;
import com.newstrust.infrastructure.adapter.in.web.openapi.AnalysisApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Traduz requisicoes HTTP em chamadas aos casos de uso do dominio. Nenhuma
 * regra de negocio vive aqui - apenas mapeamento entre DTOs e os tipos do dominio.
 * O mapeamento de rota, validacao e documentacao OpenAPI/Swagger ficam em
 * {@link AnalysisApi}, para manter este controller enxuto.
 */
@RestController
public class AnalysisController implements AnalysisApi {

    private final AnalyzeNewsContentUseCase analyzeNewsContentUseCase;
    private final AnalyzeSourceUseCase analyzeSourceUseCase;

    public AnalysisController(AnalyzeNewsContentUseCase analyzeNewsContentUseCase,
                               AnalyzeSourceUseCase analyzeSourceUseCase) {
        this.analyzeNewsContentUseCase = analyzeNewsContentUseCase;
        this.analyzeSourceUseCase = analyzeSourceUseCase;
    }

    @Override
    public ResponseEntity<NewsContentAnalysisResponse> analyzeContent(NewsContentAnalysisRequest request) {
        NewsContentSubmission submission =
                new NewsContentSubmission(request.title(), request.content(), request.sourceUrl());
        NewsAnalysis analysis = analyzeNewsContentUseCase.analyze(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(NewsContentAnalysisResponse.from(analysis));
    }

    @Override
    public ResponseEntity<SourceAnalysisResponse> analyzeSource(SourceAnalysisRequest request) {
        SourceSubmission submission = new SourceSubmission(request.url());
        SourceAnalysis analysis = analyzeSourceUseCase.analyze(submission);
        return ResponseEntity.status(HttpStatus.CREATED).body(SourceAnalysisResponse.from(analysis));
    }
}
