package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.port.out.SourceAnalysisRepositoryPort;
import com.newstrust.infrastructure.event.SourceAnalysisCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SourceAnalysisPersistenceAdapter implements SourceAnalysisRepositoryPort {

    private final SourceAnalysisJpaRepository jpaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SourceAnalysisPersistenceAdapter(SourceAnalysisJpaRepository jpaRepository,
                                             ApplicationEventPublisher eventPublisher) {
        this.jpaRepository = jpaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SourceAnalysis save(SourceAnalysis sourceAnalysis) {
        SourceAnalysisEntity saved = jpaRepository.save(SourceAnalysisEntity.fromDomain(sourceAnalysis));
        SourceAnalysis domain = saved.toDomain();
        eventPublisher.publishEvent(new SourceAnalysisCreatedEvent(domain));
        return domain;
    }

    @Override
    public Optional<SourceAnalysis> findMostRecentByDomain(String domain) {
        return jpaRepository.findFirstByDomainOrderByCreatedAtDesc(domain).map(SourceAnalysisEntity::toDomain);
    }

    @Override
    public List<SourceAnalysis> findAll() {
        return jpaRepository.findAll().stream().map(SourceAnalysisEntity::toDomain).toList();
    }
}
