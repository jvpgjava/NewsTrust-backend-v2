package com.newstrust.domain.port.out;

import com.newstrust.domain.model.NewsContentSubmission;

import java.util.OptionalDouble;

/**
 * Estima o fator D (padrao de disseminacao: velocidade/concentracao comunitaria
 * de propagacao). Retorna {@link OptionalDouble#empty()} quando nao ha dado
 * disponivel - o dominio ja trata essa ausencia com um baseline neutro
 * (ver {@link com.newstrust.domain.service.CredibilityScoreCalculator}).
 */
public interface DisseminationPatternPort {

    OptionalDouble analyze(NewsContentSubmission submission);
}
