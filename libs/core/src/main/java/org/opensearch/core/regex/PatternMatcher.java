package org.opensearch.core.regex;

public interface PatternMatcher {
    boolean matches(String input);
    boolean isMatchAllPattern();
}