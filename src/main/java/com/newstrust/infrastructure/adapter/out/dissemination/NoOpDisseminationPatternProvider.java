package com.newstrust.infrastructure.adapter.out.dissemination;

import com.newstrust.domain.model.NewsContentSubmission;
import com.newstrust.domain.port.out.DisseminationPatternPort;
import org.springframework.stereotype.Component;

import java.util.OptionalDouble;

/**
 * Implementacao inicial do fator D: o sistema ainda nao rastreia velocidade ou
 * concentracao comunitaria de propagacao (exigiria integracao com metricas de
 * compartilhamento/redes sociais, fora do escopo desta versao). Sempre retorna
 * "sem dado disponivel" - o dominio ja assume um baseline neutro nesse caso
 * (ver CredibilityScoreCalculator), em vez de distorcer o score. Trocar esta
 * classe por um adapter real nao exige nenhuma mudanca no dominio ou nos casos de uso.
 */
@Component
public class NoOpDisseminationPatternProvider implements DisseminationPatternPort {

    @Override
    public OptionalDouble analyze(NewsContentSubmission submission) {
        return OptionalDouble.empty();
    }
}
