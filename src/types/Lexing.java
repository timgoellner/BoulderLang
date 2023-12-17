package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    kwSet,

    integer,
    bool,
    identifier,

    equal,
    exclamationMarkEqual,
    less,
    lessEqual,
    greater,
    greaterEqual,

    semicolon,
    assign,
    parenthesesOpen,
    parenthesesClosed,
    curlyBracketOpen,
    curlyBracketClosed,
    plus,
    minus,
    asterisk,
    slash,
    exclamationMark,
    ampersand,
    pipe,
    backslash
  }

  public record Token(
    TokenType type,
    String value,
    int row,
    int column
  ) {} 
}