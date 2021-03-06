package com.sea.lyrad.parse.stmt;

import com.sea.lyrad.lex.Lexer;
import com.sea.lyrad.lex.analyze.UnterminatedCharException;
import com.sea.lyrad.lex.token.*;
import com.sea.lyrad.parse.BaseSQLParser;
import com.sea.lyrad.parse.SQLParseException;
import com.sea.lyrad.parse.SQLParseUnsupportedException;
import com.sea.lyrad.parse.SQLParser;
import com.sea.lyrad.stmt.SQLStatement;
import com.sea.lyrad.stmt.dal.ShowStatement;
import com.sea.lyrad.stmt.dal.UseStatement;

public class DALParser extends BaseSQLParser implements SQLParser {

    public DALParser(Lexer lexer) {
        super(lexer);
    }

    @Override
    public SQLStatement parse() throws SQLParseException, UnterminatedCharException, SQLParseUnsupportedException {
        Token token = lexer.getToken();
        if (token.getType() == Keyword.USE) {
            return parseUse();
        } else if (token.getType() == Keyword.SHOW) {
            return parseShow();
        }
        throw new SQLParseUnsupportedException(lexer.getToken().getType());
    }

    private SQLStatement parseUse() throws SQLParseException, UnterminatedCharException {
        UseStatement statement = new UseStatement(lexer.getContent());
        accept(Keyword.USE);
        Token token = lexer.getToken();
        statement.setDBName(token.getLiterals());
        accept(Literals.IDENTIFIER);
        accept(Symbol.SEMI);
        accept(Assist.END);
        return statement;
    }

    private SQLStatement parseShow() throws SQLParseException, UnterminatedCharException {
        accept(Keyword.SHOW);
        Token token = lexer.getToken();
        ShowStatement showStatement = new ShowStatement(lexer.getContent());
        if (token.getType() == Keyword.DATABASES) {
            showStatement.setItem(Keyword.DATABASES);
            accept(Keyword.DATABASES);
            accept(Symbol.SEMI);
            accept(Assist.END);
            return showStatement;
        } else if (token.getType() == Keyword.TABLES) {
            showStatement.setItem(Keyword.TABLES);
            accept(Keyword.TABLES);
            accept(Symbol.SEMI);
            accept(Assist.END);
            return showStatement;
        }
        throw new SQLParseException(lexer);
    }
}
