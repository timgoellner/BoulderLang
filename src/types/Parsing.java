package types;

import java.util.List;

import types.Lexing.Token;

public interface Parsing {
  public record NodeInteger(
    Token integer
  ) {}

  public record NodeIdentifier(
    Token identifier
  ) {}

  public record NodeParentheses(
    NodeExpression expression
  ) {}

  public record NodeTerm(
    // NodeInteger || NodeIdentifier || NodeParentheses
    Object object
  ) {}

  public record NodeExpression(
    NodeTerm term
  ) {}

  public record NodeStatementAssignment(
    Token identifier,
    NodeExpression expression
  ) {}

  public record NodeStatementSet(
    Token identifier,
    NodeExpression expression
  ) {}

  public record NodeStatementStop(
    NodeExpression expression
  ) {}

  public record NodeStatement(
    // NodeStatementSet || NodeStatementStop
    Object object
  ) {} 

  public record NodeRoot(
    List<NodeStatement> statements
  ) {} 
}