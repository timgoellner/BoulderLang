package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    kwSet,

    integer,
    bool,
    identifier,

    equal,
    notEqual,
    less,
    lessEqual,
    greater,
    greaterEqual,
    
    and,
    or,

    semicolon,
    assign,
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