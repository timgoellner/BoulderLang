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
    if (get() == null) generateError("parsing: expected expression");

    Object termObject = null;

    if (get().type() == TokenType.integer) {
      termObject = new IntegerLiteral(consume());
    } else if (get().type() == TokenType.bool) {
      termObject = new BooleanLiteral(consume());
    } else if (get().type() == TokenType.identifier) {
      termObject = new Identifier(consume());
    } else if (get().type() == TokenType.parenthesesOpen) {
      consume();
      termObject = new Parentheses(parseExpression(1));

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();
    } else if (get().type() == TokenType.minus) {
      consume();

      IntegerLiteral zeroIntegerLiteral = new IntegerLiteral(new Token(TokenType.integer, "0", -1, -1));
      Term zeroTerm = new Term(zeroIntegerLiteral);
      Expression zeroExpression = new Expression(zeroTerm);

      ExpressionBinary expressionBinary = new ExpressionBinary(ExpressionBinaryType.subtraction, zeroExpression, parseExpression(1));
      Expression expression = new Expression(expressionBinary);
      
      termObject = new Parentheses(expression);
    } else if (get().type() == TokenType.exclamationMark) {
      consume();

      Term term = parseTerm();
      termObject = new TermNegated(term);
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
    } else if (get().type() == TokenType.kwSet) {
      consume();

      if (get() == null || get().type() != TokenType.identifier) generateError("parsing: expected identifier");
      Token identifier = consume();

      if (get()!= null && get().type() == TokenType.semicolon) {
        consume();
        StatementSet statementSet = new StatementSet(identifier, null);
        return new Statement(statementSet);
      }

      if (get() == null || get().type() != TokenType.assign) generateError("parsing: expected equal sign");
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementSet statementSet = new StatementSet(identifier, expression);

      return new Statement(statementSet);
    } else if (get().type() == TokenType.identifier) {
      Token identifier = consume();

      if (get() == null || get().type() != TokenType.assign) generateError("parsing: expected equal sign");
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) generateError("parsing: expected ';'");
      consume();

      StatementAssignment statementAssignment = new StatementAssignment(identifier, expression);

      return new Statement(statementAssignment);
    } else if (get().type() == TokenType.parenthesesOpen) {
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.parenthesesClosed) generateError("parsing: expected ')'");
      consume();

      Statement statement = parseStatement();
      Statement statementElse = null;

      if (get().type() == TokenType.backslash) {
        consume();
        statementElse = parseStatement();
      }

      Branch statementCondition = new Branch(expression, statement, statementElse);

      return new Statement(statementCondition);
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
    String column = (token == null) ? "EOF" : token.column() + "";

    System.out.println(row + ":" + column + ": ERROR: " + msg);
    System.exit(1);
  }
}
