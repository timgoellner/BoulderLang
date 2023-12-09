import java.util.ArrayList;
import java.util.List;
import java.util.function.BinaryOperator;

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

    Object TermObject = null;

    if (get().type() == TokenType.integer) {
      TermObject = new IntegerLiteral(consume());
    } else if (get().type() == TokenType.identifier) {
      TermObject = new Identifier(consume());
    } else if (get().type() == TokenType.parenthesesOpen) {
      consume();
      TermObject = new Parentheses(parseExpression(1));

      if (!(get().type() == TokenType.parenthesesClosed) || get() == null) {
        System.out.println("parsing: expected ')'");
        System.exit(1);
      }
      consume();
    }
    
    if (TermObject == null) {
      System.out.println("parsing: expected term");
      System.exit(1);
    }

    return new Term(TermObject);
  }

  private Expression parseExpression(int minPrecedence) {
    Term termLeft = parseTerm();

    Expression expressionLeft = new Expression(termLeft);

    while (true) {
      Token currToken = get();
      if (currToken == null || getPrecedence(currToken.type()) < minPrecedence) break;

      TokenType opertator = consume().type();
      int nextMinPrecedence = getPrecedence(opertator) + 1;

      Expression expressionRight = parseExpression(nextMinPrecedence);

      ExpressionBinary expressionBinary = null;
      if (opertator == TokenType.plus) {
        expressionBinary = new ExpressionBinary(ExpressionBinaryType.addition, expressionLeft, expressionRight);
      } else if (opertator == TokenType.minus) {
        expressionBinary = new ExpressionBinary(ExpressionBinaryType.subtraction, expressionLeft, expressionRight);
      } else  if (opertator == TokenType.asterisk) {
        expressionBinary = new ExpressionBinary(ExpressionBinaryType.multiplication, expressionLeft, expressionRight);
      } else  if (opertator == TokenType.slash) {
        expressionBinary = new ExpressionBinary(ExpressionBinaryType.division, expressionLeft, expressionRight);
      }

      expressionLeft = new Expression(expressionBinary);
    }

    return expressionLeft;
  }

  private Statement parseStatement() {
    if (get().type() == TokenType.kwStop) {
      consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) {
        System.out.println("parsing: expected '('");
        System.exit(1);
      }
      consume();

      Expression Expression = parseExpression(1);

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

      StatementStop StatementStop = new StatementStop(Expression);

      return new Statement(StatementStop);
    } else if (get().type() == TokenType.kwSet) {
      consume();

      if (get() == null || get().type() != TokenType.identifier) {
        System.out.println("parsing: expected identifier");
        System.exit(1);
      }
      Token identifier = consume();

      if (get() == null || get().type() != TokenType.equal) {
        System.out.println("parsing: expected equal sign");
        System.exit(1);
      }
      consume();

      Expression Expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      StatementSet StatementSet = new StatementSet(identifier, Expression);

      return new Statement(StatementSet);
    } else if (get().type() == TokenType.identifier) {
      Token identifier = consume();

      if (get() == null || get().type() != TokenType.equal) {
        System.out.println("parsing: expected equal sign");
        System.exit(1);
      }
      consume();

      Expression Expression = parseExpression(1);

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      StatementAssignment StatementAssignment = new StatementAssignment(identifier, Expression);

      return new Statement(StatementAssignment);
    }

    System.out.println("parsing: invalid statement");
    return null;
  }

  public Root parse() {
    List<Statement> statements = new ArrayList<Statement>();
    
    while (tokens.size() > 0) {
      Statement statement = parseStatement();
      if (statement == null) System.exit(1);
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
      case plus:
      case minus:
        return 1;
      case asterisk:
      case slash:
        return 2;
      default:
        return 0;
    }
  }
}
