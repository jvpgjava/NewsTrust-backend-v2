package com.newstrust.domain.port.out;

import com.newstrust.domain.model.NewsAnalysis;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistencia das analises de noticia. Implementado pelo adapter JPA/pgvector.
 */
public interface NewsAnalysisRepositoryPort {

    NewsAnalysis save(NewsAnalysis newsAnalysis);

    Optional<NewsAnalysis> findById(UUID id);

    List<NewsAnalysis> findAll();
}
