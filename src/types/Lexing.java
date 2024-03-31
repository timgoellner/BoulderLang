package types;

public interface Lexing {
  public enum TokenType {
    kwStop,
    kwPrint,

    integer,
    string,
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
    squareBracketOpen,
    squareBracketClosed,
    curlyBracketOpen,
    curlyBracketClosed,
    plus,
    minus,
    asterisk,
    slash,
    exclamationMark,
    ampersand,
    pipe,
    backslash,
    tilde,
    arrow,
    comma,
    degreeSign
  }

  public record Token(
    TokenType type,
    String value,
    int row,
    int column
  ) {} 
}