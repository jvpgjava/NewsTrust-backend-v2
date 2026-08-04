package com.newstrust.infrastructure.adapter.out.persistence;

import java.util.UUID;

/**
 * Projecao da busca nativa por similaridade de cosseno (id + similaridade),
 * usada para preservar a ordem de relevancia antes de hidratar as entidades completas.
 */
public interface NewsSimilarityProjection {

    UUID getId();

    Double getSimilarity();
}
