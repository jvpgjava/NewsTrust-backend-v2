package com.newstrust.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NewsAnalysisJpaRepository extends JpaRepository<NewsAnalysisEntity, UUID> {

    /**
     * pgvector cosine distance (`<=>`) e 1 - similaridade_de_cosseno para vetores
     * normalizados; o HNSW index em news_analysis_embedding_hnsw_idx acelera o ORDER BY.
     * O vetor de consulta chega como o literal textual "[v1,v2,...]" do pgvector,
     * convertido via CAST para evitar depender de um tipo JDBC customizado no parametro.
     */
    @Query(value = """
            SELECT id, (1 - (embedding <=> CAST(:embeddingLiteral AS vector))) AS similarity
            FROM news_analysis
            ORDER BY embedding <=> CAST(:embeddingLiteral AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<NewsSimilarityProjection> findNearestByEmbedding(@Param("embeddingLiteral") String embeddingLiteral,
                                                           @Param("limit") int limit);
}
