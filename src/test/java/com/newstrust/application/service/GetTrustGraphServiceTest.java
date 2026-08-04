package com.newstrust.application.service;

import com.newstrust.domain.model.GraphData;
import com.newstrust.domain.port.out.TrustGraphRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTrustGraphServiceTest {

    @Mock
    private TrustGraphRepositoryPort trustGraphRepositoryPort;

    @Test
    void delegatesEachMethodToTheRepositoryPort() {
        GetTrustGraphService service = new GetTrustGraphService(trustGraphRepositoryPort);
        GraphData sources = new GraphData(List.of(), List.of());
        GraphData news = new GraphData(List.of(), List.of());
        UUID newsId = UUID.randomUUID();

        when(trustGraphRepositoryPort.sourceNetwork()).thenReturn(sources);
        when(trustGraphRepositoryPort.newsNetwork()).thenReturn(news);
        when(trustGraphRepositoryPort.newsNodeDetail(newsId)).thenReturn(Optional.empty());

        assertThat(service.sourceNetwork()).isSameAs(sources);
        assertThat(service.newsNetwork()).isSameAs(news);
        assertThat(service.newsNodeDetail(newsId)).isEmpty();
    }
}
