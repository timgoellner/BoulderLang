package types;

import java.util.List;

import types.Lexing.Token;

public interface Parsing {
  public record NodeExpression(
    Token integer
  ) {}

  public record NodeStatementStop(
    NodeExpression expression
  ) {}

  public record NodeStatement(
    Object statement
  ) {} 

  public record NodeRoot(
    List<NodeStatement> statements
  ) {} 
}