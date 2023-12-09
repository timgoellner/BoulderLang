import java.util.ArrayList;
import java.util.List;

import types.Lexing.*;
import types.Parsing.*;

public class Parser {
  private List<Token> tokens;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }


  private NodeTerm parseTerm() {
    Object nodeTermObject = null;

    if (get().type() == TokenType.integer) {
      nodeTermObject = new NodeInteger(consume());
    } else if (get().type() == TokenType.identifier) {
      nodeTermObject = new NodeIdentifier(consume());
    } else if (get().type() == TokenType.parenthesesOpen) {
      consume();
      nodeTermObject = new NodeParentheses(parseExpression());

      if (!(get().type() == TokenType.parenthesesClosed) || get() == null) {
        System.out.println("parsing: expected ')'");
        System.exit(1);
      }
      consume();
    }
    
    if (nodeTermObject == null) {
      System.out.println("parsing: expected term");
      System.exit(1);
    }

    return new NodeTerm(nodeTermObject);
  }

  private NodeExpression parseExpression() {
    if (get() == null) {
      System.out.println("parsing: expected expression");
      System.exit(1);
    }

    return new NodeExpression(parseTerm());
  }

  private NodeStatement parseStatement() {
    if (get().type() == TokenType.kwStop) {
      consume();

      if (get() == null || get().type() != TokenType.parenthesesOpen) {
        System.out.println("parsing: expected '('");
        System.exit(1);
      }
      consume();

      NodeExpression nodeExpression = parseExpression();

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

      NodeStatementStop nodeStatementStop = new NodeStatementStop(nodeExpression);

      return new NodeStatement(nodeStatementStop);
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

      NodeExpression nodeExpression = parseExpression();

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      NodeStatementSet nodeStatementSet = new NodeStatementSet(identifier, nodeExpression);

      return new NodeStatement(nodeStatementSet);
    } else if (get().type() == TokenType.identifier) {
      Token identifier = consume();

      if (get() == null || get().type() != TokenType.equal) {
        System.out.println("parsing: expected equal sign");
        System.exit(1);
      }
      consume();

      NodeExpression nodeExpression = parseExpression();

      if (get() == null || get().type() != TokenType.semicolon) {
        System.out.println("parsing: expected ';'");
        System.exit(1);
      }
      consume();

      NodeStatementAssignment nodeStatementAssignment = new NodeStatementAssignment(identifier, nodeExpression);

      return new NodeStatement(nodeStatementAssignment);
    }

    System.out.println("parsing: invalid statement");
    return null;
  }

  public NodeRoot parse() {
    List<NodeStatement> statements = new ArrayList<NodeStatement>();
    
    while (tokens.size() > 0) {
      NodeStatement statement = parseStatement();
      if (statement == null) System.exit(1);
      statements.add(statement);
    }

    return new NodeRoot(statements);
  }


  private Token get() {
    if (tokens.size() == 0) return null;
    return tokens.get(0);
  }

  private Token consume() {
    return tokens.remove(0);
  }
}
