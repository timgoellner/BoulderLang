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
    if (get() == null) {
      System.out.println("parsing: expected expression");
      System.exit(1);
    }

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

      if (get() == null || get().type() != TokenType.parenthesesClosed) {
        System.out.println("parsing: expected ')'");
        System.exit(1);
      }
      consume();
    } else if (get().type() == TokenType.minus) {
      consume();

      IntegerLiteral zeroIntegerLiteral = new IntegerLiteral(new Token(TokenType.integer, "0"));
      Term zeroTerm = new Term(zeroIntegerLiteral);
      Expression zeroExpression = new Expression(zeroTerm);

      ExpressionBinary expressionBinary = new ExpressionBinary(ExpressionBinaryType.subtraction, zeroExpression, parseExpression(1));
      Expression expression = new Expression(expressionBinary);
      
      termObject = new Parentheses(expression);
    }
    
    if (termObject == null) {
      System.out.println("parsing: expected term");
      System.exit(1);
    }

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
      else if (opertator == TokenType.notEqual) expressionBinary = new ExpressionBinary(ExpressionBinaryType.notEqual, expressionLeft, expressionRight);
      else if (opertator == TokenType.less) expressionBinary = new ExpressionBinary(ExpressionBinaryType.less, expressionLeft, expressionRight);
      else if (opertator == TokenType.lessEqual) expressionBinary = new ExpressionBinary(ExpressionBinaryType.lessEqual, expressionLeft, expressionRight);
      else if (opertator == TokenType.greater) expressionBinary = new ExpressionBinary(ExpressionBinaryType.greater, expressionLeft, expressionRight);
      else if (opertator == TokenType.greaterEqual) expressionBinary = new ExpressionBinary(ExpressionBinaryType.greaterEqual, expressionLeft, expressionRight);

      else if (opertator == TokenType.and) expressionBinary = new ExpressionBinary(ExpressionBinaryType.and, expressionLeft, expressionRight);
      else if (opertator == TokenType.or) expressionBinary = new ExpressionBinary(ExpressionBinaryType.or, expressionLeft, expressionRight);

      expressionLeft = new Expression(expressionBinary);
    }

    return expressionLeft;
  }

  private Statement parseStatement() {
    if (get().type() == TokenType.curlyBracketOpen) {
      consume();

      List<Statement> statements = new ArrayList<Statement>();
      Statement statement;
      while ((statement = parseStatement()) != null) statements.add(statement);

      if (get() == null || get().type() != TokenType.curlyBracketClosed) {
        System.out.println("parsing: expected '}'");
        System.exit(1);
      }
      consume();

      Scope scope = new Scope(statements);

      return new Statement(scope);
    } else if (get().type() == TokenType.kwStop) {
      consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) {
        System.out.println("parsing: expected '('");
        System.exit(1);
      }
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.parenthesesClosed) {
        System.out.println("parsing: expected ')'");
        System.exit(1);
      }
      consume();

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      StatementStop statementStop = new StatementStop(expression);

      return new Statement(statementStop);
    } else if (get().type() == TokenType.kwSet) {
      consume();

      if (get() == null || get().type() != TokenType.identifier) {
        System.out.println("parsing: expected identifier");
        System.exit(1);
      }
      Token identifier = consume();

      if (get()!= null && get().type() == TokenType.semicolon) {
        consume();
        StatementSet statementSet = new StatementSet(identifier, null);
        return new Statement(statementSet);
      }

      if (get() == null || get().type() != TokenType.assign) {
        System.out.println("parsing: expected equal sign");
        System.exit(1);
      }
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      StatementSet statementSet = new StatementSet(identifier, expression);

      return new Statement(statementSet);
    } else if (get().type() == TokenType.identifier) {
      Token identifier = consume();

      if (get() == null || get().type() != TokenType.assign) {
        System.out.println("parsing: expected equal sign");
        System.exit(1);
      }
      consume();

      Expression expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      StatementAssignment statementAssignment = new StatementAssignment(identifier, expression);

      return new Statement(statementAssignment);
    }

    return null;
  }

  public Root parse() {
    List<Statement> statements = new ArrayList<Statement>();
    
    while (tokens.size() > 0) {
      Statement statement = parseStatement();
      if (statement == null) {
        System.out.println("parsing: invalid statement");
        System.exit(1);
      }
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
      case and:
      case or:
        return 1;
      case equal:
      case notEqual:
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
}
