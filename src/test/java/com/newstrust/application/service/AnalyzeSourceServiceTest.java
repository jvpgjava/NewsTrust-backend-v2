package com.newstrust.application.service;

import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.model.SourceReputation;
import com.newstrust.domain.model.SourceSubmission;
import com.newstrust.domain.port.out.SourceAnalysisRepositoryPort;
import com.newstrust.domain.port.out.SourceReputationPort;
import com.newstrust.domain.service.CredibilityScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyzeSourceServiceTest {

    @Mock
    private SourceReputationPort sourceReputationPort;
    @Mock
    private SourceAnalysisRepositoryPort sourceAnalysisRepositoryPort;

    private AnalyzeSourceService service;

    @BeforeEach
    void setUp() {
        service = new AnalyzeSourceService(sourceReputationPort, sourceAnalysisRepositoryPort,
                new CredibilityScoreCalculator());
    }

    @Test
    void extractsDomainFromUrlAndScoresPurelyOnReputation() {
        SourceSubmission submission = new SourceSubmission("https://www.example.com/section/article");

        when(sourceReputationPort.lookup("example.com"))
                .thenReturn(new SourceReputation("example.com", 82.0, "confiavel", List.of("dominio antigo")));
        when(sourceAnalysisRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SourceAnalysis result = service.analyze(submission);

        assertThat(result.domain()).isEqualTo("example.com");
        assertThat(result.score().value()).isCloseTo(82.0, org.assertj.core.data.Offset.offset(1e-9));

        ArgumentCaptor<SourceAnalysis> captor = ArgumentCaptor.forClass(SourceAnalysis.class);
        verify(sourceAnalysisRepositoryPort).save(captor.capture());
        assertThat(captor.getValue().url()).isEqualTo("https://www.example.com/section/article");
    }
}
