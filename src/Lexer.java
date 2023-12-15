import java.util.ArrayList;
import java.util.List;

import types.Lexing.*;

public class Lexer {
  private String raw;

  public Lexer(String raw) {
    this.raw = raw;
  }

  public List<Token> tokenize() {
    List<Token> tokens = new ArrayList<Token>();
    String buffer = "";

    while (raw.length() > 0) {
      char currChar = consume();

      if (Character.isAlphabetic(currChar) ||  Character.isDigit(currChar)) buffer += currChar;
      else if (buffer.length() > 0) {
        switch (buffer) {
          case "stop":
            tokens.add(new Token(TokenType.kwStop, null));
            buffer = "";
            break;
          case "set":
            tokens.add(new Token(TokenType.kwSet, null));
            buffer = "";
            break;
          case "true":
            tokens.add(new Token(TokenType.bool, "true"));
            buffer = "";
            break;
          case "false":
            tokens.add(new Token(TokenType.bool, "false"));
            buffer = "";
            break;
        }

        try {
          Integer.parseInt(buffer);

          tokens.add(new Token(TokenType.integer, buffer));
          buffer = "";
        } catch (NumberFormatException error) {}

        if (!buffer.isEmpty()) {
          tokens.add(new Token(TokenType.identifier, buffer));
          buffer = "";
        }
      }

      switch (currChar) {
        case '=':
          if (get() == '=') {
            tokens.add(new Token(TokenType.equal, null));
            consume();
          }
          else tokens.add(new Token(TokenType.assign, null));
          break;
        case '!':
          if (get() == '=') {
            tokens.add(new Token(TokenType.notEqual, null));
            consume();
          }
          // TODO else tokens.add(new Token(TokenType.not, null));
          break;
        case '<':
          if (get() == '=') {
            tokens.add(new Token(TokenType.lessEqual, null));
            consume();
          }
          else tokens.add(new Token(TokenType.less, null));
          break;
        case '>':
          if (get() == '=') {
            tokens.add(new Token(TokenType.greaterEqual, null));
            consume();
          }
          else tokens.add(new Token(TokenType.greater, null));
          break;
        
        case '&':
          tokens.add(new Token(TokenType.and, null));
          break;
        case '|':
          tokens.add(new Token(TokenType.or, null));
          break;
        case ';':
          tokens.add(new Token(TokenType.semicolon, null));
          break;
        case '(':
          tokens.add(new Token(TokenType.parenthesesOpen, null));
          break;
        case ')':
          tokens.add(new Token(TokenType.parenthesesClosed, null));
          break;
        case '{':
          tokens.add(new Token(TokenType.curlyBracketOpen, null));
          break;
        case '}':
          tokens.add(new Token(TokenType.curlyBracketClosed, null));
          break;
        case '+':
          tokens.add(new Token(TokenType.plus, null));
          break;
        case '-':
          tokens.add(new Token(TokenType.minus, null));
          break;
        case '*':
          tokens.add(new Token(TokenType.asterisk, null));
          break;
        case '/':
          tokens.add(new Token(TokenType.slash, null));
          break;
      }
    }

    return tokens;
  }


  private char get() {
    return raw.charAt(0);
  }

  private char consume() {
    char value = get();
    raw = raw.substring(1);
    return value;
  }
}
