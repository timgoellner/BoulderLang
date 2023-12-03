import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import types.Lexing.Token;
import types.Parsing.NodeRoot;

public class Main {
  public static void main(String args[]) {
    if (args.length == 0 || !args[0].endsWith(".boulder")) {
      System.out.print("please provide a valid file (?.boulder)");
      return;
    } 

    Path filePath = Path.of(args[0]);
    String content;
    try { content = Files.readString(filePath); }
    catch (IOException error) {
      System.out.print("error reading file " + args[0] + "\nplease provide a valid file (?.boulder)");
      return;
    }

    Lexer lexer = new Lexer(content);
    List<Token> tokens = lexer.tokenize();

    Parser parser = new Parser(tokens);
    NodeRoot nodeRoot = parser.parse();

    Generator generator = new Generator(nodeRoot);
    String output = generator.generate();

    filePath = Path.of("bin/output.asm");
    try {
      if(!Files.exists(filePath)) Files.writeString(filePath, output, StandardOpenOption.CREATE);
      else Files.writeString(filePath, output, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException error) {
      System.out.print("error writing to file");
      return;
    }

    Runtime runntime = Runtime.getRuntime();
    try {
      runntime.exec("nasm -felf64 bin/output.asm");
      runntime.exec("ld -o bin/output.exe bin/output.o");
    } catch (IOException error) {
      System.out.print("error compiling assembly");
      return;
    }
  }
}