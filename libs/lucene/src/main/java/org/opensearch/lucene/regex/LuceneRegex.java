package org.opensearch.lucene.regex;

import org.apache.lucene.util.automaton.Automata;
import org.apache.lucene.util.automaton.Automaton;
import org.apache.lucene.util.automaton.Operations;
import org.opensearch.common.Glob;
import org.opensearch.core.common.Strings;
import org.opensearch.core.regex.PatternMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class LuceneRegex implements PatternMatcher {

    public static final int UNICODE_CHARACTER_CLASS = 0x100;

    public static boolean isSimpleMatchPattern(String str) {
        return str.indexOf('*') != -1;
    }

    @Override
    public boolean isMatchAllPattern() {
        return false; // Implementation specific
    }

    @Override
    public boolean matches(String input) {
        return false; // Implementation specific
    }

    public static Automaton simpleMatchToAutomaton(String pattern) {
        List<Automaton> automata = new ArrayList<>();
        int previous = 0;
        for (int i = pattern.indexOf('*'); i != -1; i = pattern.indexOf('*', i + 1)) {
            automata.add(Automata.makeString(pattern.substring(previous, i)));
            automata.add(Automata.makeAnyString());
            previous = i + 1;
        }
        automata.add(Automata.makeString(pattern.substring(previous)));
        return Operations.concatenate(automata);
    }

    public static Automaton simpleMatchToAutomaton(String... patterns) {
        if (patterns.length < 1) {
            throw new IllegalArgumentException("There must be at least one pattern, zero given");
        }
        List<Automaton> automata = new ArrayList<>();
        for (String pattern : patterns) {
            automata.add(simpleMatchToAutomaton(pattern));
        }
        return Operations.union(automata);
    }

    public static boolean simpleMatch(String pattern, String str) {
        return simpleMatch(pattern, str, false);
    }

    public static boolean simpleMatch(String pattern, String str, boolean caseInsensitive) {
        if (pattern == null || str == null) {
            return false;
        }
        if (caseInsensitive) {
            pattern = Strings.toLowercaseAscii(pattern);
            str = Strings.toLowercaseAscii(str);
        }
        return Glob.globMatch(pattern, str);
    }

    public static boolean simpleMatch(String[] patterns, String str) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (simpleMatch(pattern, str)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean simpleMatch(final List<String> patterns, final String str) {
        return patterns != null && simpleMatch(patterns.toArray(Strings.EMPTY_ARRAY), str);
    }

    public static Pattern compile(String regex, String flags) {
        int pFlags = flags == null ? 0 : flagsFromString(flags);
        return Pattern.compile(regex, pFlags);
    }

    public static int flagsFromString(String flags) {
        int pFlags = 0;
        for (String s : Strings.delimitedListToStringArray(flags, "|")) {
            if (s.isEmpty()) {
                continue;
            }
            s = s.toUpperCase(Locale.ROOT);
            if ("CASE_INSENSITIVE".equals(s)) {
                pFlags |= Pattern.CASE_INSENSITIVE;
            } else if ("MULTILINE".equals(s)) {
                pFlags |= Pattern.MULTILINE;
            } else if ("DOTALL".equals(s)) {
                pFlags |= Pattern.DOTALL;
            } else if ("UNICODE_CASE".equals(s)) {
                pFlags |= Pattern.UNICODE_CASE;
            } else if ("CANON_EQ".equals(s)) {
                pFlags |= Pattern.CANON_EQ;
            } else if ("UNIX_LINES".equals(s)) {
                pFlags |= Pattern.UNIX_LINES;
            } else if ("LITERAL".equals(s)) {
                pFlags |= Pattern.LITERAL;
            } else if ("COMMENTS".equals(s)) {
                pFlags |= Pattern.COMMENTS;
            } else if (("UNICODE_CHAR_CLASS".equals(s)) || ("UNICODE_CHARACTER_CLASS".equals(s))) {
                pFlags |= UNICODE_CHARACTER_CLASS;
            } else {
                throw new IllegalArgumentException("Unknown regex flag [" + s + "]");
            }
        }
        return pFlags;
    }
}