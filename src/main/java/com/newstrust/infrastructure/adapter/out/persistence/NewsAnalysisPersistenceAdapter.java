package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.port.out.NewsAnalysisRepositoryPort;
import com.newstrust.infrastructure.event.NewsAnalysisCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class NewsAnalysisPersistenceAdapter implements NewsAnalysisRepositoryPort {

    private final NewsAnalysisJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public NewsAnalysisPersistenceAdapter(NewsAnalysisJpaRepository jpaRepository,
                                           ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public NewsAnalysis save(NewsAnalysis newsAnalysis) {
        NewsAnalysisEntity saved = jpaRepository.save(NewsAnalysisEntity.fromDomain(newsAnalysis));
        NewsAnalysis domain = saved.toDomain();
        eventPublisher.publishEvent(new NewsAnalysisCreatedEvent(domain));
        return domain;
    }

    @Override
    public Optional<NewsAnalysis> findById(UUID id) {
        return jpaRepository.findById(id).map(NewsAnalysisEntity::toDomain);
    }

    @Override
    public List<NewsAnalysis> findAll() {
        return jpaRepository.findAll().stream().map(NewsAnalysisEntity::toDomain).toList();
    }
}
