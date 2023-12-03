package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    parenthesesOpen,
    parenthesesClosed,
    integer,
    semicolon
  }

  public record Token(
    TokenType type,
    String value
  ) {} 
}