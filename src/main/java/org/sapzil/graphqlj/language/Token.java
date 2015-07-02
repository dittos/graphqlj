package org.sapzil.graphqlj.language;

/**
 * A representation of a lexed Token. Value is optional, is it is
 * not needed for punctuators like BANG or PAREN_L.
 */
public final class Token {
    private final TokenKind kind;
    private final int start;
    private final int end;
    private final String value; // nullable

    public Token(TokenKind kind, int start, int end) {
        this(kind, start, end, null);
    }

    public Token(TokenKind kind, int start, int end, String value) {
        this.kind = kind;
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public TokenKind getKind() {
        return kind;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getValue() {
        return value;
    }
}
