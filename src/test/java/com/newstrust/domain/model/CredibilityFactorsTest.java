package com.newstrust.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CredibilityFactorsTest {

    @Test
    void withoutDisseminationDataLeavesDComponentEmpty() {
        CredibilityFactors factors = CredibilityFactors.withoutDisseminationData(80, 60, 90);
        assertTrue(factors.disseminationPattern().isEmpty());
    }

    @Test
    void ofPopulatesAllFourFactors() {
        CredibilityFactors factors = CredibilityFactors.of(80, 60, 90, 70);
        assertTrue(factors.disseminationPattern().isPresent());
    }

    @Test
    void rejectsSourceReputationOutOfRange() {
        assertThrows(IllegalArgumentException.class,
                () -> CredibilityFactors.withoutDisseminationData(101, 60, 90));
        assertThrows(IllegalArgumentException.class,
                () -> CredibilityFactors.withoutDisseminationData(-1, 60, 90));
    }

    @Test
    void rejectsDisseminationOutOfRangeWhenPresent() {
        assertThrows(IllegalArgumentException.class, () -> CredibilityFactors.of(80, 60, 90, 150));
    }

    @Test
    void rejectsNullDisseminationOptional() {
        assertThrows(IllegalArgumentException.class, () -> new CredibilityFactors(80, 60, 90, null));
    }

    @Test
    void acceptsBoundaryValuesZeroAndOneHundred() {
        assertDoesNotThrow(() -> CredibilityFactors.of(0, 0, 0, 0));
        assertDoesNotThrow(() -> CredibilityFactors.of(100, 100, 100, 100));
    }
}
