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

  public record StringLiteral(
    Token string
  ) {}

  public record BooleanLiteral(
    Token bool
  ) {}

  public record ArrayLiteral(
    Term length
  ) {}

  public record Identifier(
    Token identifier
  ) {}

  public record ArrayIdentifier(
    Token identifier,
    Expression offset
  ) {}

  public record Parentheses(
    Expression expression
  ) {}

  public record TermNegated(
    Term term
  ) {}

  public record Term(
    // IntegerLiteral || StringLiteral || BooleanLiteral || ArrayLiteral || Identifier || ArrayIdentifier || Parentheses || TermNegated || Call
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

  public record Loop(
    Expression condition,
    Statement statement
  ) {}

  public record StatementAssignment(
    Term identifier,
    Expression value
  ) {}

  public record StatementPrint(
    Expression expression
  ) {}

  public record StatementInitialize(
    Token identifier,
    Expression value
  ) {}

  public record StatementStop(
    Expression expression
  ) {}

  public record Scope(
    List<Statement> statements
  ) {}

  public record Method(
    Token identifier,
    List<Token> parameters,
    Statement statement
  ) {}

  public record Call(
    Token identifier,
    List<Expression> parameters
  ) {}

  public record StatementReturn(
    Expression expression
  ) {}

  public record Statement(
    // StatementSet || StatementPrint || StatementStop || StatementAssignment || Scope || Branch || Loop || Method || StatementReturn || StatementCall
    Object object
  ) {}

  public record Root(
    List<Statement> statements
  ) {} 
}