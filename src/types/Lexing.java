package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    kwSet,
    semicolon,
    equal,
    parenthesesOpen,
    parenthesesClosed,
    plus,
    minus,
    asterisk,
    slash,
    identifier,
    integer
  }

  public record Token(
    TokenType type,
    String value
  ) {} 
}