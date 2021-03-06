package com.sea.lyrad.lex.token;


/**
 * Token
 */
public class Token {

    private TokenType type;
    private String literals;
    private int endPosition;

    public Token(TokenType type, String literals, int endPosition) {
        this.type = type;
        this.literals = literals;
        this.endPosition = endPosition;
    }

    public TokenType getType() {
        return type;
    }

    public String getLiterals() {
        return literals;
    }

    public int getEndPosition() {
        return endPosition;
    }

    @Override
    public String toString() {
        return "Token{" +
                "type=" + type +
                ", literals='" + literals + '\'' +
                '}';
    }
}
