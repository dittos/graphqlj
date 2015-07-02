package org.sapzil.graphqlj.language;

/**
 * An enum describing the different kinds of tokens that the lexer omits.
 */
public enum TokenKind {
    EOF("EOF"),
    BANG("!"),
    DOLLAR("$"),
    PAREN_L("("),
    PAREN_R(")"),
    SPREAD("..."),
    COLON(":"),
    EQUALS("="),
    AT("@"),
    BRACKET_L("["),
    BRACKET_R("]"),
    BRACE_L("{"),
    PIPE("|"),
    BRACE_R("}"),
    NAME("Name"),
    VARIABLE("Variable"),
    INT("Int"),
    FLOAT("Float"),
    STRING("String");

    private final String desc;

    TokenKind(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
