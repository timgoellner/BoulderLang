package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    kwSet,

    integer,
    identifier,

    semicolon,
    equal,
    parenthesesOpen,
    parenthesesClosed,
    curlyBracketOpen,
    curlyBracketClosed,
    plus,
    minus,
    asterisk,
    slash
  }

  public record Token(
    TokenType type,
    String value
  ) {} 
}