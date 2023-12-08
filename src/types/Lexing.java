package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    kwSet,
    semicolon,
    equal,
    parenthesesOpen,
    parenthesesClosed,
    identifier,
    integer
  }

  public record Token(
    TokenType type,
    String value
  ) {} 
}