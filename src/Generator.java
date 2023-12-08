import java.util.ArrayList;
import java.util.List;

import types.Parsing.*;
import types.Generating.*;

public class Generator {
  private NodeRoot nodeRoot;
  private String output;

  private int stackSize;
  private List<Variable> variables;

  public Generator(NodeRoot nodeRoot) {
    this.nodeRoot = nodeRoot;
    this.output = "global _start\n_start:\n";

    this.variables = new ArrayList<Variable>();
  }


  private void push(String register) {
    output += "    push " + register + "\n";
    stackSize++;
  }

  private void pop(String register) {
    output += "    pop " + register + "\n";
    stackSize--;
  }


  private void generateExpression(NodeExpression nodeExpression) {
    if (nodeExpression.object().getClass() == NodeInteger.class) {
      output += "    mov rax, " + ((NodeInteger) nodeExpression.object()).integer().value() + "\n";
      push("rax");
    } else if (nodeExpression.object().getClass() == NodeIdentifier.class) {
      Variable variable = null;
      for (Variable testVariable : variables) {
        if (variable != null) break;
        if (testVariable.name().matches(((NodeIdentifier) nodeExpression.object()).identifier().value())) variable = testVariable;
      }

      if (variable == null) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      String offset = "QWORD [rsp + " + ((stackSize - variable.stackLocation() - 1) * 8) + "]";
      push(offset);
    }
  }

  private void generateStatement(NodeStatement nodeStatement) {
    if (nodeStatement.object().getClass() == NodeStatementStop.class) {
      generateExpression(((NodeStatementStop) nodeStatement.object()).expression());
      output += "    mov rax, 60\n";
      pop("rdi");
      output += "    syscall\n";
    } else if (nodeStatement.object().getClass() == NodeStatementSet.class) {
      NodeStatementSet nodeStatementSet = (NodeStatementSet) nodeStatement.object();

      variables.add(new Variable(nodeStatementSet.identifier().value(), stackSize));
      generateExpression(nodeStatementSet.expression());
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
