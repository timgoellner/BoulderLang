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

    int row = 1;
    int column = 0;

    boolean inString = false;

    while (raw.length() > 0) {
      char currChar = consume();

      if (Character.isAlphabetic(currChar) ||  Character.isDigit(currChar) || inString || currChar == '_') buffer += currChar;
      else if (buffer.length() > 0) {
        switch (buffer) {
          case "stop":
            tokens.add(new Token(TokenType.kwStop, null, row, column));
            buffer = "";
            break;
          case "print":
            tokens.add(new Token(TokenType.kwPrint, null, row, column));
            buffer = "";
            break;
          case "true":
            tokens.add(new Token(TokenType.bool, "true", row, column));
            buffer = "";
            break;
          case "false":
            tokens.add(new Token(TokenType.bool, "false", row, column));
            buffer = "";
            break;
        }

        try {
          Double.parseDouble(buffer);

          tokens.add(new Token(TokenType.integer, buffer, row, column));
          buffer = "";
        } catch (NumberFormatException error) {}

        if (!buffer.isEmpty()) {
          tokens.add(new Token(TokenType.identifier, buffer, row, column));
          buffer = "";
        }
      }

      if (inString) {
        if (currChar == '"') {
          tokens.add(new Token(TokenType.string, buffer.substring(0, buffer.length()-1), row, column));
          buffer = "";
          inString = false;
        }
        continue;
      }

      switch (currChar) {
        case '=':
          if (get() == '=') {
            tokens.add(new Token(TokenType.equal, null, row, column));
            consume();
          }
          else tokens.add(new Token(TokenType.assign, null, row, column));
          break;
        case '!':
          if (get() == '=') {
            tokens.add(new Token(TokenType.exclamationMarkEqual, null, row, column));
            consume();
          }
          else tokens.add(new Token(TokenType.exclamationMark, null, row, column));
          break;
        case '<':
          if (get() == '=') {
            tokens.add(new Token(TokenType.lessEqual, null, row, column));
            consume();
          }
          else tokens.add(new Token(TokenType.less, null, row, column));
          break;
        case '>':
          if (get() == '=') {
            tokens.add(new Token(TokenType.greaterEqual, null, row, column));
            consume();
          }
          else tokens.add(new Token(TokenType.greater, null, row, column));
          break;
        
        case '&':
          tokens.add(new Token(TokenType.ampersand, null, row, column));
          break;
        case '|':
          tokens.add(new Token(TokenType.pipe, null, row, column));
          break;
        case ';':
          tokens.add(new Token(TokenType.semicolon, null, row, column));
          break;
        case '(':
          tokens.add(new Token(TokenType.parenthesesOpen, null, row, column));
          break;
        case ')':
          tokens.add(new Token(TokenType.parenthesesClosed, null, row, column));
          break;
        case '[':
          tokens.add(new Token(TokenType.squareBracketOpen, null, row, column));
          break;
        case ']':
          tokens.add(new Token(TokenType.squareBracketClosed, null, row, column));
          break;
        case '{':
          tokens.add(new Token(TokenType.curlyBracketOpen, null, row, column));
          break;
        case '}':
          tokens.add(new Token(TokenType.curlyBracketClosed, null, row, column));
          break;
        case '+':
          tokens.add(new Token(TokenType.plus, null, row, column));
          break;
        case '-':
          if (get() == '>') {
            consume();
            tokens.add(new Token(TokenType.arrow, null, row, column));
            break;
          }
          tokens.add(new Token(TokenType.minus, null, row, column));
          break;
        case '*':
          tokens.add(new Token(TokenType.asterisk, null, row, column));
          break;
        case '/':
          tokens.add(new Token(TokenType.slash, null, row, column));
          break;
        case '\\':
          tokens.add(new Token(TokenType.backslash, null, row, column));
          break;
        case '~':
          tokens.add(new Token(TokenType.tilde, null, row, column));
          break;
        case '"':
          inString = true;
          break;
        case ',':
          tokens.add(new Token(TokenType.comma, null, row, column));
          break;
        case '°':
          tokens.add(new Token(TokenType.degreeSign, null, row, column));
          break;
        case '#':
          if (get() == '*') {
            consume();
            while (raw.length() > 0 && (currChar != '*' || get() != '#')) currChar = consume();
            if (raw.length() > 0) consume();
          }
          else while (currChar != '\n' && raw.length() > 0) currChar = consume();
          break;
      }

      if (currChar == '\n') { row++; column = 0; }
      else column++;
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
