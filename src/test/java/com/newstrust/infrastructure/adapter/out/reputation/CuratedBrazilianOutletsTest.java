package com.newstrust.infrastructure.adapter.out.reputation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CuratedBrazilianOutletsTest {

    private final CuratedBrazilianOutlets curatedOutlets = new CuratedBrazilianOutlets();

    @Test
    void recognizesAKnownOutletFromTheCuratedList() {
        assertThat(curatedOutlets.reputationFor("g1.globo.com"))
                .hasValue(CuratedBrazilianOutlets.CURATED_REPUTATION_SCORE);
    }

    @Test
    void isCaseInsensitive() {
        assertThat(curatedOutlets.reputationFor("G1.GLOBO.COM"))
                .hasValue(CuratedBrazilianOutlets.CURATED_REPUTATION_SCORE);
    }

    @Test
    void returnsEmptyForADomainNotInTheList() {
        assertThat(curatedOutlets.reputationFor("blog-suspeito-qualquer.com")).isEmpty();
    }

    @Test
    void returnsEmptyForNullDomain() {
        assertThat(curatedOutlets.reputationFor(null)).isEmpty();
    }

    @Test
    void includesTheFactCheckingAgenciesCitedInTheArticle() {
        assertThat(curatedOutlets.reputationFor("aosfatos.org")).isPresent();
        assertThat(curatedOutlets.reputationFor("lupa.uol.com.br")).isPresent();
    }
}
