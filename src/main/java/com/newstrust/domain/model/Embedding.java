package com.newstrust.domain.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Vetor de embedding de um texto (titulo+conteudo de noticia), usado para busca
 * por similaridade de cosseno via pgvector. O record nao sabe nada sobre pgvector
 * ou Spring AI - e apenas o dado.
 */
public record Embedding(float[] vector) {

    public Embedding {
        Objects.requireNonNull(vector, "vector nao pode ser nulo");
        if (vector.length == 0) {
            throw new IllegalArgumentException("vector nao pode ser vazio");
        }
        vector = vector.clone();
    }

    public int dimensions() {
        return vector.length;
    }

    @Override
    public float[] vector() {
        return vector.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Embedding other)) {
            return false;
        }
        return Arrays.equals(vector, other.vector);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vector);
    }

    @Override
    public String toString() {
        return "Embedding[dimensions=" + vector.length + "]";
    }
}
