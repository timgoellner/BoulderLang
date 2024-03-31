import java.util.ArrayList;
import java.util.List;

import types.Lexing.*;
import types.Parsing.*;

public class Parser {
  private List<Token> tokens;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }


  private Term parseTerm() {
    if (get() == null) generateError("parsing: expected term");

    Object termObject = null;

    if (get().type() == TokenType.integer) {
      termObject = new IntegerLiteral(consume());
    } else if (get().type() == TokenType.string) {
      termObject = new StringLiteral(consume());
    } else if (get().type() == TokenType.bool) {
      termObject = new BooleanLiteral(consume());
    } else if (get().type() == TokenType.identifier) {
      Token identifier = consume();

      if (get() != null && get().type() == TokenType.squareBracketOpen) {
        consume();
        termObject = new ArrayIdentifier(identifier, parseExpression(1));

        if (get() == null || get().type() != TokenType.squareBracketClosed) generateError("parsing: expected ']'");
        consume();
      } else if (get() != null && get().type() == TokenType.parenthesesOpen) {
        consume();

        List<Expression> parameters = new ArrayList<Expression>();
        while(true) {
          if (get().type() == TokenType.parenthesesClosed) break;
          parameters.add(parseExpression(1));
          if (get().type() == TokenType.parenthesesClosed) break;
          if (get().type() != TokenType.comma) generateError("parsing: expected ')'");
          consume();
        }
        consume();

        termObject = new Call(identifier, parameters);
      } else termObject = new Identifier(identifier);
    } else if (get().type() == TokenType.parenthesesOpen) {
      consume();
      termObject = new Parentheses(parseExpression(1));

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();
    } else if (get().type() == TokenType.minus) {
      consume();

      IntegerLiteral zeroIntegerLiteral = new IntegerLiteral(new Token(TokenType.integer, "0", get().row(), get().column()));
      Term zeroTerm = new Term(zeroIntegerLiteral);
      Expression zeroExpression = new Expression(zeroTerm);

      ExpressionBinary expressionBinary = new ExpressionBinary(ExpressionBinaryType.subtraction, zeroExpression, parseExpression(1));
      Expression expression = new Expression(expressionBinary);
      
      termObject = new Parentheses(expression);
    } else if (get().type() == TokenType.exclamationMark) {
      consume();

      Term term = parseTerm();
      termObject = new TermNegated(term);
    } else if (get().type() == TokenType.squareBracketOpen) {
      consume();
      termObject = new ArrayLiteral(parseTerm());

      if (get() == null || get().type() != TokenType.squareBracketClosed) generateError("parsing: expected ']'");
      consume();
    }
    
    if (termObject == null) generateError("parsing: expected term");

    return new Term(termObject);
  }

  private Expression parseExpression(int minPrecedence) {
    Expression expressionLeft = new Expression(parseTerm());

    while (true) {
      Token currToken = get();
      if (currToken == null || getPrecedence(currToken.type()) < minPrecedence) break;

      TokenType opertator = consume().type();
      int nextMinPrecedence = getPrecedence(opertator) + 1;

      Expression expressionRight = parseExpression(nextMinPrecedence);

      ExpressionBinary expressionBinary = null;
      if (opertator == TokenType.plus) expressionBinary = new ExpressionBinary(ExpressionBinaryType.addition, expressionLeft, expressionRight);
      else if (opertator == TokenType.minus) expressionBinary = new ExpressionBinary(ExpressionBinaryType.subtraction, expressionLeft, expressionRight);
      else if (opertator == TokenType.asterisk) expressionBinary = new ExpressionBinary(ExpressionBinaryType.multiplication, expressionLeft, expressionRight);
      else if (opertator == TokenType.slash) expressionBinary = new ExpressionBinary(ExpressionBinaryType.division, expressionLeft, expressionRight);
      
      else if (opertator == TokenType.equal) expressionBinary = new ExpressionBinary(ExpressionBinaryType.equal, expressionLeft, expressionRight);
      else if (opertator == TokenType.exclamationMarkEqual) expressionBinary = new ExpressionBinary(ExpressionBinaryType.notEqual, expressionLeft, expressionRight);
      else if (opertator == TokenType.less) expressionBinary = new ExpressionBinary(ExpressionBinaryType.less, expressionLeft, expressionRight);
      else if (opertator == TokenType.lessEqual) expressionBinary = new ExpressionBinary(ExpressionBinaryType.lessEqual, expressionLeft, expressionRight);
      else if (opertator == TokenType.greater) expressionBinary = new ExpressionBinary(ExpressionBinaryType.greater, expressionLeft, expressionRight);
      else if (opertator == TokenType.greaterEqual) expressionBinary = new ExpressionBinary(ExpressionBinaryType.greaterEqual, expressionLeft, expressionRight);

      else if (opertator == TokenType.ampersand) expressionBinary = new ExpressionBinary(ExpressionBinaryType.and, expressionLeft, expressionRight);
      else if (opertator == TokenType.pipe) expressionBinary = new ExpressionBinary(ExpressionBinaryType.or, expressionLeft, expressionRight);

      expressionLeft = new Expression(expressionBinary);
    }

    return expressionLeft;
  }

  private Statement parseStatement() {
    if (get() == null) return null;

    if (get().type() == TokenType.curlyBracketOpen) {
      consume();

      List<Statement> statements = new ArrayList<Statement>();
      Statement statement;
      while ((statement = parseStatement()) != null) statements.add(statement);

      if (get() == null || get().type() != TokenType.curlyBracketClosed) generateError("parsing: expected '}'");
      consume();

      Scope scope = new Scope(statements);

      return new Statement(scope);
    } else if (get().type() == TokenType.kwStop) {
      consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) generateError("parsing: expected '('");
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementStop statementStop = new StatementStop(expression);

      return new Statement(statementStop);
    } else if (get().type() == TokenType.greater) {
      consume();

      if (get() == null || get().type() != TokenType.identifier) generateError("parsing: expected identifier");
      Token identifier = consume();

      if (get()!= null && get().type() == TokenType.semicolon) {
        consume();
        StatementInitialize statementInitialize = new StatementInitialize(identifier, null);
        return new Statement(statementInitialize);
      }

      if (get() == null || get().type() != TokenType.assign) generateError("parsing: expected equal sign");
      consume();

      Expression value = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementInitialize statementInitialize = new StatementInitialize(identifier, value);

      return new Statement(statementInitialize);
    } else if (get().type() == TokenType.kwPrint) {
      consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) generateError("parsing: expected '('");
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementPrint statementPrint = new StatementPrint(expression);

      return new Statement(statementPrint);
    } else if (get().type() == TokenType.identifier) {
      Term identifier = parseTerm();

      if (identifier.object() instanceof Call call) {
        if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
        consume();

        return new Statement(call);
      }

      if (get() == null || get().type() != TokenType.assign) generateError("parsing: expected equal sign");
      consume();

      Expression value = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementAssignment statementAssignment = new StatementAssignment(identifier, value);
      return new Statement(statementAssignment);
    } else if (get().type() == TokenType.parenthesesOpen) {
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();

      Statement statement = parseStatement();
      Statement statementElse = null;

      if (get() != null && get().type() == TokenType.backslash) {
        consume();
        statementElse = parseStatement();
      }

      Branch branch = new Branch(expression, statement, statementElse);

      return new Statement(branch);
    } else if (get().type() == TokenType.tilde) {
      consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) generateError("parsing: expected '('");
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();

      Statement statement = parseStatement();

      Loop loop = new Loop(expression, statement);

      return new Statement(loop);
    } else if (get().type() == TokenType.arrow) {
      consume();

      Expression expression = null;
      if (get().type() != TokenType.semicolon) expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementReturn statementReturn = new StatementReturn(expression);
      return new Statement(statementReturn);
    } else if (get().type() == TokenType.degreeSign) {
      consume();

      if (get() == null || get().type() != TokenType.identifier) generateError("parsing: expected identifier");
      Token identifier = consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) generateError("parsing: expected '('");
      consume();

      List<Token> parameters = new ArrayList<Token>();
      while(get().type() == TokenType.identifier) {
        parameters.add(consume());
        if (get().type() == TokenType.comma) consume();
        if (get().type() != TokenType.parenthesesClosed && get().type() != TokenType.identifier) generateError("parsing: expected ')'");
      }
      consume();

      Statement statement = parseStatement();

      Method method = new Method(identifier, parameters, statement);
      return new Statement(method);
    }

    return null;
  }

  public Root parse() {
    List<Statement> statements = new ArrayList<Statement>();
    
    while (tokens.size() > 0) {
      Statement statement = parseStatement();
      if (statement == null) generateError("parsing: invalid statement");
      statements.add(statement);
    }

    return new Root(statements);
  }


  private Token get() {
    if (tokens.size() == 0) return null;
    return tokens.get(0);
  }

  private Token consume() {
    return tokens.remove(0);
  }

  private int getPrecedence(TokenType tokenType) {
    switch (tokenType) {
      case ampersand:
      case pipe:
        return 1;
      case equal:
      case exclamationMarkEqual:
      case less:
      case lessEqual:
      case greater:
      case greaterEqual:
        return 2;
      case plus:
      case minus:
        return 3;
      case asterisk:
      case slash:
        return 4;
      default:
        return 0;
    }
  }

  private void generateError(String msg) {
    Token token = get();
    String row = (token == null) ? "EOF" : token.row() + "";
    String column = (token == null) ? "EOF" : token.column() + 1 + "";

    System.out.println(row + ":" + column + ": ERROR: " + msg);
    System.exit(1);
  }
}
