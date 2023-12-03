import types.Parsing.*;

public class Generator {
  private NodeRoot nodeRoot;
  private String output;

  public Generator(NodeRoot nodeRoot) {
    this.nodeRoot = nodeRoot;
    this.output = "global _start\n_start:\n";
  }

  private void generateExpression(NodeExpression nodeExpression) {
    output += "    mov rax, " + nodeExpression.integer().value() + "\n";
    output += "    push rax\n";
  }

  private void generateStatement(NodeStatement nodeStatement) {
    if (nodeStatement.statement().getClass() == NodeStatementStop.class) {
      generateExpression(((NodeStatementStop) nodeStatement.statement()).expression());
      output += "    mov rax, 60\n";
      output += "    pop rdi\n";
      output += "    syscall\n";
    }
  }

  public String generate() {
    for (NodeStatement nodeStatement : nodeRoot.statements()) {
      generateStatement(nodeStatement);
    }

    output += "    mov rax, 60\n";
    output += "    mov rdi, 0\n";
    output += "    syscall\n";

    return output;
  }
}
