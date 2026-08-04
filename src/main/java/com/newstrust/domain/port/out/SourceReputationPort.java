package com.newstrust.domain.port.out;

import com.newstrust.domain.model.SourceReputation;

/**
 * Consulta a reputacao de um dominio (hoje via ScamAdviser). Alimenta o fator R.
 */
public interface SourceReputationPort {

    SourceReputation lookup(String domain);
}
