package types;

import java.util.List;

import types.Lexing.Token;

public interface Parsing {
  public enum ExpressionBinaryType {
    addition,
    subtraction,
    multiplication,
    division
  }

  public record IntegerLiteral(
    Token integer
  ) {}

  public record Identifier(
    Token identifier
  ) {}

  public record Parentheses(
    Expression expression
  ) {}

  public record Term(
    // IntegerLiteral || Identifier || Parentheses
    Object object
  ) {}

  public record ExpressionBinary(
    ExpressionBinaryType type,
    Expression expressionLeft,
    Expression expressionRight
  ) {}

  public record Expression(
    // Term || ExpressionBinary
    Object object
  ) {}

  public record StatementAssignment(
    Token identifier,
    Expression expression
  ) {}

  public record StatementSet(
    Token identifier,
    Expression expression
  ) {}

  public record StatementStop(
    Expression expression
  ) {}

  public record Statement(
    // StatementSet || StatementStop || StatementAssignment || Scope
    Object object
  ) {}

  public record Scope(
    List<Statement> statements
  ) {}

  public record Root(
    List<Statement> statements
  ) {} 
}