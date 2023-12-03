import java.util.ArrayList;
import java.util.List;

import types.Lexing.*;
import types.Parsing.*;

public class Parser {
  private List<Token> tokens;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }


  private NodeExpression parseExpression() {
    if (get() == null || get().type() != TokenType.integer) {
      System.out.println("parsing: expected int");
      System.exit(1);
    }

    return new NodeExpression(consume());
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
    }

    System.out.println("Invalid Statement");
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
