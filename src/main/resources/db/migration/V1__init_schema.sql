CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE news_analysis (
    id                          UUID PRIMARY KEY,
    title                       TEXT NOT NULL,
    content                     TEXT NOT NULL,
    source_url                  TEXT,
    embedding                   VECTOR(768) NOT NULL,
    score_value                 DOUBLE PRECISION NOT NULL,
    risk_level                  VARCHAR(20) NOT NULL,
    reputation_weight           DOUBLE PRECISION NOT NULL,
    textual_consistency_weight  DOUBLE PRECISION NOT NULL,
    cross_verification_weight   DOUBLE PRECISION NOT NULL,
    dissemination_weight        DOUBLE PRECISION NOT NULL,
    source_reputation_factor    DOUBLE PRECISION NOT NULL,
    textual_consistency_factor  DOUBLE PRECISION NOT NULL,
    cross_verification_factor   DOUBLE PRECISION NOT NULL,
    dissemination_factor        DOUBLE PRECISION NOT NULL,
    dissemination_is_baseline   BOOLEAN NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Busca aproximada de vizinhos mais proximos por similaridade de cosseno (RAG).
CREATE INDEX news_analysis_embedding_hnsw_idx ON news_analysis
    USING hnsw (embedding vector_cosine_ops);

CREATE INDEX news_analysis_risk_level_idx ON news_analysis (risk_level);
CREATE INDEX news_analysis_created_at_idx ON news_analysis (created_at DESC);

CREATE TABLE news_analysis_reason (
    news_analysis_id UUID NOT NULL REFERENCES news_analysis (id) ON DELETE CASCADE,
    position         INTEGER NOT NULL,
    factor           VARCHAR(40) NOT NULL,
    description      TEXT NOT NULL,
    PRIMARY KEY (news_analysis_id, position)
);

CREATE TABLE news_analysis_ai_explanation (
    news_analysis_id UUID NOT NULL REFERENCES news_analysis (id) ON DELETE CASCADE,
    position         INTEGER NOT NULL,
    explanation      TEXT NOT NULL,
    PRIMARY KEY (news_analysis_id, position)
);

CREATE TABLE source_analysis (
    id                          UUID PRIMARY KEY,
    domain                      TEXT NOT NULL,
    url                         TEXT NOT NULL,
    reputation_score            DOUBLE PRECISION NOT NULL,
    reputation_category         VARCHAR(60) NOT NULL,
    score_value                 DOUBLE PRECISION NOT NULL,
    risk_level                  VARCHAR(20) NOT NULL,
    reputation_weight           DOUBLE PRECISION NOT NULL,
    textual_consistency_weight  DOUBLE PRECISION NOT NULL,
    cross_verification_weight   DOUBLE PRECISION NOT NULL,
    dissemination_weight        DOUBLE PRECISION NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX source_analysis_domain_idx ON source_analysis (domain);
CREATE INDEX source_analysis_created_at_idx ON source_analysis (created_at DESC);

CREATE TABLE source_analysis_signal (
    source_analysis_id UUID NOT NULL REFERENCES source_analysis (id) ON DELETE CASCADE,
    position            INTEGER NOT NULL,
    signal              TEXT NOT NULL,
    PRIMARY KEY (source_analysis_id, position)
);

CREATE TABLE source_analysis_reason (
    source_analysis_id UUID NOT NULL REFERENCES source_analysis (id) ON DELETE CASCADE,
    position            INTEGER NOT NULL,
    factor              VARCHAR(40) NOT NULL,
    description         TEXT NOT NULL,
    PRIMARY KEY (source_analysis_id, position)
);
