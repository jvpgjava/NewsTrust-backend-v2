package com.newstrust.domain.port.out;

import com.newstrust.domain.model.SourceAnalysis;

import java.util.List;
import java.util.Optional;

/**
 * Persistencia das analises de fonte. Implementado pelo adapter JPA.
 */
public interface SourceAnalysisRepositoryPort {

    SourceAnalysis save(SourceAnalysis sourceAnalysis);

    Optional<SourceAnalysis> findMostRecentByDomain(String domain);

    List<SourceAnalysis> findAll();
}
