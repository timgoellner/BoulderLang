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
        }

        try {
          Integer.parseInt(buffer);

          tokens.add(new Token(TokenType.integer, buffer));
          buffer = "";
        } catch (NumberFormatException error) {}
      }

      switch (currChar) {
        case '(':
          tokens.add(new Token(TokenType.parenthesesOpen, null));
          break;
        case ')':
          tokens.add(new Token(TokenType.parenthesesClosed, null));
          break;
        case ';':
          tokens.add(new Token(TokenType.semicolon, null));
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
