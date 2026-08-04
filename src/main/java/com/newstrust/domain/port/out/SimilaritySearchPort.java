package com.newstrust.domain.port.out;

import com.newstrust.domain.model.Embedding;
import com.newstrust.domain.model.SimilarNewsMatch;

import java.util.List;

/**
 * Busca as noticias ja processadas mais proximas de um embedding, por similaridade
 * de cosseno (pgvector/HNSW). E a base do pipeline RAG: quanto mais noticias
 * processadas, mais rico o contexto retornado aqui.
 */
public interface SimilaritySearchPort {

    List<SimilarNewsMatch> findMostSimilar(Embedding embedding, int limit);
}
