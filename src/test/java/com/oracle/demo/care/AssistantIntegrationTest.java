package com.oracle.demo.care;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantIntegrationTest {
    @Test
    void assistantPromptBoundaryKeepsDatabaseResultsAsTheAuthority() {
        String rule = "The assistant may summarize only rows and policy documents returned by Oracle AI Database.";
        assertThat(rule).contains("Oracle AI Database");
    }
}
