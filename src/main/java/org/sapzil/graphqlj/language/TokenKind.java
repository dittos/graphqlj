package org.sapzil.graphqlj.language;

/**
 * An enum describing the different kinds of tokens that the lexer omits.
 */
public enum TokenKind {
    EOF,
    BANG,
    DOLLAR,
    PAREN_L,
    PAREN_R,
    SPREAD,
    COLON,
    EQUALS,
    AT,
    BRACKET_L,
    BRACKET_R,
    BRACE_L,
    PIPE,
    BRACE_R,
    NAME,
    VARIABLE,
    INT,
    FLOAT,
    STRING,
}
