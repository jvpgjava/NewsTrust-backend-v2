package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.GraphData;
import com.newstrust.domain.model.GraphEdge;
import com.newstrust.domain.model.GraphNode;
import com.newstrust.domain.model.NewsAnalysis;
import com.newstrust.domain.model.NewsGraphNodeDetail;
import com.newstrust.domain.model.SourceAnalysis;
import com.newstrust.domain.port.out.TrustGraphRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Constroi as duas visoes do grafo de credibilidade a partir do que ja foi
 * persistido. As arestas nao dependem de embeddings adicionais para fontes
 * (nao ha texto de dominio a comparar): a similaridade entre fontes e definida
 * como proximidade dos seus scores de credibilidade. Para noticias, a
 * similaridade e a de conteudo (cosseno via pgvector), reaproveitando o mesmo
 * indice HNSW usado na analise de conteudo.
 */
@Component
public class TrustGraphPersistenceAdapter implements TrustGraphRepositoryPort {

    private static final double SOURCE_CREDIBILITY_SIMILARITY_THRESHOLD = 15.0;
    private static final int NEWS_NEIGHBORS_PER_NODE = 5;
    private static final double NEWS_CONTENT_SIMILARITY_THRESHOLD = 0.75;

    private final SourceAnalysisJpaRepository sourceAnalysisJpaRepository;
    private final NewsAnalysisJpaRepository newsAnalysisJpaRepository;

    public TrustGraphPersistenceAdapter(SourceAnalysisJpaRepository sourceAnalysisJpaRepository,
                                         NewsAnalysisJpaRepository newsAnalysisJpaRepository) {
        this.sourceAnalysisJpaRepository = sourceAnalysisJpaRepository;
        this.newsAnalysisJpaRepository = newsAnalysisJpaRepository;
    }

    @Override
    public GraphData sourceNetwork() {
        List<SourceAnalysis> analyses = sourceAnalysisJpaRepository.findLatestPerDomain().stream()
                .map(SourceAnalysisEntity::toDomain)
                .toList();

        List<GraphNode> nodes = analyses.stream()
                .map(a -> new GraphNode(a.domain(), a.domain(), a.score().value(), a.score().riskLevel(),
                        Map.of("url", a.url(), "reputationCategory", a.reputation().category())))
                .toList();

        List<GraphEdge> edges = new ArrayList<>();
        for (int i = 0; i < analyses.size(); i++) {
            for (int j = i + 1; j < analyses.size(); j++) {
                double diff = Math.abs(analyses.get(i).score().value() - analyses.get(j).score().value());
                if (diff <= SOURCE_CREDIBILITY_SIMILARITY_THRESHOLD) {
                    double weight = 1.0 - (diff / 100.0);
                    edges.add(new GraphEdge(analyses.get(i).domain(), analyses.get(j).domain(), weight));
                }
            }
        }

        return new GraphData(nodes, edges);
    }

    @Override
    public GraphData newsNetwork() {
        List<NewsAnalysis> analyses = newsAnalysisJpaRepository.findAll().stream()
                .map(NewsAnalysisEntity::toDomain)
                .toList();

        List<GraphNode> nodes = analyses.stream()
                .map(a -> new GraphNode(a.id().toString(), a.title(), a.score().value(), a.score().riskLevel(),
                        Map.of("sourceUrl", a.sourceUrl() == null ? "" : a.sourceUrl())))
                .toList();

        List<GraphEdge> edges = buildNewsContentSimilarityEdges(analyses);

        return new GraphData(nodes, edges);
    }

    @Override
    public Optional<NewsGraphNodeDetail> newsNodeDetail(UUID newsId) {
        return newsAnalysisJpaRepository.findById(newsId).map(entity -> {
            NewsAnalysis analysis = entity.toDomain();
            List<GraphEdge> connections = newsNetwork().edges().stream()
                    .filter(edge -> edge.sourceId().equals(newsId.toString()) || edge.targetId().equals(newsId.toString()))
                    .toList();
            return new NewsGraphNodeDetail(analysis.id(), analysis.title(), analysis.content(), analysis.score(),
                    analysis.createdAt(), connections);
        });
    }

    private List<GraphEdge> buildNewsContentSimilarityEdges(List<NewsAnalysis> analyses) {
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();

        for (NewsAnalysis analysis : analyses) {
            String literal = PgVectorLiterals.toLiteral(analysis.embedding());
            List<NewsSimilarityProjection> neighbors =
                    newsAnalysisJpaRepository.findNearestByEmbedding(literal, NEWS_NEIGHBORS_PER_NODE + 1);

            for (NewsSimilarityProjection neighbor : neighbors) {
                if (neighbor.getId().equals(analysis.id())) {
                    continue;
                }
                if (neighbor.getSimilarity() < NEWS_CONTENT_SIMILARITY_THRESHOLD) {
                    continue;
                }
                String pairKey = pairKey(analysis.id(), neighbor.getId());
                if (!seenPairs.add(pairKey)) {
                    continue;
                }
                edges.add(new GraphEdge(analysis.id().toString(), neighbor.getId().toString(), neighbor.getSimilarity()));
            }
        }
        return edges;
    }

    private static String pairKey(UUID a, UUID b) {
        String sa = a.toString();
        String sb = b.toString();
        return sa.compareTo(sb) < 0 ? sa + "|" + sb : sb + "|" + sa;
    }
}
