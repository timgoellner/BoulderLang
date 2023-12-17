package types;

import java.util.List;

import types.Lexing.Token;

public interface Parsing {
  public enum ExpressionBinaryType {
    addition,
    subtraction,
    multiplication,
    division,

    equal,
    notEqual,
    less,
    lessEqual,
    greater,
    greaterEqual,
    
    and,
    or
  }

  public record IntegerLiteral(
    Token integer
  ) {}

  public record BooleanLiteral(
    Token bool
  ) {}

  public record Identifier(
    Token identifier
  ) {}

  public record Parentheses(
    Expression expression
  ) {}

  public record TermNegated(
    Term term
  ) {}

  public record Term(
    // IntegerLiteral || BooleanLiteral || Identifier || Parentheses || TermNegated
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

  public record Branch(
    Expression condition,
    Statement statement,
    Statement statementElse
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

  public record Scope(
    List<Statement> statements
  ) {}

  public record Statement(
    // StatementSet || StatementStop || StatementAssignment || Scope || Branch
    Object object
  ) {}

  public record Root(
    List<Statement> statements
  ) {} 
}