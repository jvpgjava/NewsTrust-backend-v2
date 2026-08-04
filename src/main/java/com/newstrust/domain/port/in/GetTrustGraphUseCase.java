package com.newstrust.domain.port.in;

import com.newstrust.domain.model.GraphData;
import com.newstrust.domain.model.NewsGraphNodeDetail;

import java.util.Optional;
import java.util.UUID;

public interface GetTrustGraphUseCase {

    GraphData sourceNetwork();

    GraphData newsNetwork();

    Optional<NewsGraphNodeDetail> newsNodeDetail(UUID newsId);
}
