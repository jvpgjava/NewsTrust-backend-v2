package com.newstrust.infrastructure.adapter.out.reputation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record ScamAdviserResponse(
        @JsonProperty("trust_score") Integer trustScore,
        @JsonProperty("risk_factors") List<String> riskFactors
) {
}
