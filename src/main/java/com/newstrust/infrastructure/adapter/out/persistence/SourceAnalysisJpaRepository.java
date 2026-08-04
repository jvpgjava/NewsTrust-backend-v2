package com.newstrust.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SourceAnalysisJpaRepository extends JpaRepository<SourceAnalysisEntity, UUID> {

    Optional<SourceAnalysisEntity> findFirstByDomainOrderByCreatedAtDesc(String domain);

    /**
     * Uma linha por dominio (a analise mais recente), usada para montar a rede de fontes.
     */
    @Query(value = "SELECT DISTINCT ON (domain) * FROM source_analysis ORDER BY domain, created_at DESC",
            nativeQuery = true)
    List<SourceAnalysisEntity> findLatestPerDomain();
}
