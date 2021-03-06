package com.sea.lyrad.lex;

import com.sea.lyrad.lex.analyze.Tokenizer;
import com.sea.lyrad.lex.analyze.UnterminatedCharException;
import com.sea.lyrad.lex.token.Assist;
import com.sea.lyrad.lex.token.Token;
import com.sea.lyrad.parse.SQLParseException;
import com.sea.lyrad.util.CharUtil;

/**
 * 词法分析器
 */
public class Lexer {

    private String content;
    private int position;
    private Token token;
    private Tokenizer tokenizer;

    public Lexer(String content) {
        this.content = content;
        tokenizer = new Tokenizer(content);
        position = 0;
    }

    public String getContent() {
        return content;
    }

    /**
     * @return 当前词素
     */
    public Token getToken() {
        return token;
    }

    private char charAt(int position) {
        return this.position + position >= content.length() ? CharUtil.getEOI() : content.charAt(this.position + position);
    }

    /**
     * 移动pos至下一词素
     *
     * @return 下一词素
     * @throws UnterminatedCharException 有开始却无匹配的结束字符（串）
     * @throws SQLParseException         无法解析的词素类型
     */
    public Token nextToken() throws UnterminatedCharException, SQLParseException {
        skipIgnoredToken();
        if (isVariableBegin()) {
            token = tokenizer.eatVariable(position);
        } else if (isIdentifierBegin()) {
            token = tokenizer.eatIdentifier(position);
        } else if (isHexDecimalBegin()) {
            token = tokenizer.eatHexDecimal(position);
        } else if (isNumberBegin()) {
            token = tokenizer.eatNumber(position);
        } else if (isSymbolBegin()) {
            token = tokenizer.eatSymbol(position);
        } else if (isStringBegin()) {
            token = tokenizer.eatString(position);
        } else if (isEnd()) {
            token = new Token(Assist.END, "", position);
        } else {
            throw new SQLParseException(this, Assist.ERROR);
        }
        position = token.getEndPosition();
        return token;
    }

    /**
     * 跳过无意义词素
     *
     * @throws UnterminatedCharException 有开始却无匹配的结束字符（串）
     */
    private void skipIgnoredToken() throws UnterminatedCharException {
        position = tokenizer.eatWhitespace(position);
        while (isHintBegin()) {
            position = tokenizer.eatHint(position);
            position = tokenizer.eatWhitespace(position);
        }
        while (isCommentBegin()) {
            position = tokenizer.eatComment(position);
            position = tokenizer.eatWhitespace(position);
        }
    }

    private boolean isHintBegin() {
        return '/' == charAt(0) && '*' == charAt(1) && '!' == charAt(2);
    }

    private boolean isCommentBegin() {
        char current = charAt(0);
        char next = charAt(1);
        return '#' == current || '/' == current && '/' == next || '-' == current && '-' == next || '/' == current && '*' == next;
    }

    private boolean isVariableBegin() {
        return '@' == charAt(0);
    }

    private boolean isIdentifierBegin() {
        return isIdentifierBegin(charAt(0));
    }

    private boolean isIdentifierBegin(char ch) {
        return CharUtil.isAlphabet(ch) || '`' == ch || '_' == ch || '$' == ch;
    }

    private boolean isHexDecimalBegin() {
        return '0' == charAt(0) && 'x' == charAt(1);
    }

    private boolean isNumberBegin() {
        return CharUtil.isDigital(charAt(0)) || ('.' == charAt(0) && CharUtil.isDigital(charAt(1)) && !isIdentifierBegin(charAt(-1))
                || ('-' == charAt(0) && ('.' == charAt(1) || CharUtil.isDigital(charAt(1)))));
    }

    private boolean isSymbolBegin() {
        return CharUtil.isSymbol(charAt(0));
    }

    private boolean isStringBegin() {
        return '\'' == charAt(0) || '\"' == charAt(0);
    }

    private boolean isEnd() {
        return position >= content.length();
    }
}
