package com.newstrust.infrastructure.adapter.out.persistence;

import com.newstrust.domain.model.Embedding;

/**
 * Converte um {@link Embedding} para o literal textual "[v1,v2,...]" que o
 * pgvector aceita via CAST(:param AS vector) em queries nativas.
 */
final class PgVectorLiterals {

    private PgVectorLiterals() {
    }

    static String toLiteral(Embedding embedding) {
        float[] vector = embedding.vector();
        StringBuilder builder = new StringBuilder(vector.length * 8 + 2);
        builder.append('[');
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(vector[i]);
        }
        builder.append(']');
        return builder.toString();
    }
}
