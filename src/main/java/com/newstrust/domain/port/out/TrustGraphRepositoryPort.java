package com.newstrust.domain.port.out;

import com.newstrust.domain.model.GraphData;
import com.newstrust.domain.model.NewsGraphNodeDetail;

import java.util.Optional;
import java.util.UUID;

/**
 * Consultas de leitura que constroem as duas visoes do grafo de credibilidade:
 * rede de fontes (nos = dominios) e rede de noticias (nos = noticias, coloridas
 * por faixa de risco).
 */
public interface TrustGraphRepositoryPort {

    GraphData sourceNetwork();

    GraphData newsNetwork();

    Optional<NewsGraphNodeDetail> newsNodeDetail(UUID newsId);
}
