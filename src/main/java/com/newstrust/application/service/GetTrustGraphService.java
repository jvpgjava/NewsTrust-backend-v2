package com.newstrust.application.service;

import com.newstrust.domain.model.GraphData;
import com.newstrust.domain.model.NewsGraphNodeDetail;
import com.newstrust.domain.port.in.GetTrustGraphUseCase;
import com.newstrust.domain.port.out.TrustGraphRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class GetTrustGraphService implements GetTrustGraphUseCase {

    private final TrustGraphRepositoryPort trustGraphRepositoryPort;

    public GetTrustGraphService(TrustGraphRepositoryPort trustGraphRepositoryPort) {
        this.trustGraphRepositoryPort = trustGraphRepositoryPort;
    }

    @Override
    public GraphData sourceNetwork() {
        return trustGraphRepositoryPort.sourceNetwork();
    }

    @Override
    public GraphData newsNetwork() {
        return trustGraphRepositoryPort.newsNetwork();
    }

    @Override
    public Optional<NewsGraphNodeDetail> newsNodeDetail(UUID newsId) {
        return trustGraphRepositoryPort.newsNodeDetail(newsId);
    }
}
