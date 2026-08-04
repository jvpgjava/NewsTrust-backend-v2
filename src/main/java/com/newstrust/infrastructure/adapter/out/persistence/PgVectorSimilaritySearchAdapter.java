package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.model.RiskLevel;
import com.newstrust.domain.model.SimilarNewsMatch;
import com.newstrust.domain.port.out.SimilaritySearchPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PgVectorSimilaritySearchAdapter implements SimilaritySearchPort {

    private final NewsAnalysisJpaRepository jpaRepository;

    public PgVectorSimilaritySearchAdapter(NewsAnalysisJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<SimilarNewsMatch> findMostSimilar(Embedding embedding, int limit) {
        List<NewsSimilarityProjection> projections =
                jpaRepository.findNearestByEmbedding(PgVectorLiterals.toLiteral(embedding), limit);

        if (projections.isEmpty()) {
            return List.of();
        }

        List<UUID> ids = projections.stream().map(NewsSimilarityProjection::getId).toList();
        Map<UUID, NewsAnalysisEntity> entitiesById = jpaRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(NewsAnalysisEntity::getId, Function.identity()));

        List<SimilarNewsMatch> matches = new ArrayList<>(projections.size());
        for (NewsSimilarityProjection projection : projections) {
            NewsAnalysisEntity entity = entitiesById.get(projection.getId());
            if (entity == null) {
                continue;
            }
            matches.add(new SimilarNewsMatch(
                    entity.getId(),
                    entity.getTitle(),
                    clampToUnitInterval(projection.getSimilarity()),
                    RiskLevel.fromScore(entity.getScoreValue())));
        }
        return matches;
    }

    private static double clampToUnitInterval(double similarity) {
        return Math.max(0.0, Math.min(1.0, similarity));
    }
}
