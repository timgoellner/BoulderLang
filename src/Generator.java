import types.Parsing.*;

import java.util.HashMap;
import java.util.Map;

public class Generator {
  private NodeRoot nodeRoot;
  private String output;

  private int stackSize;
  //          name  , stackLocation
  private Map<String, Integer> variables;

  public Generator(NodeRoot nodeRoot) {
    this.nodeRoot = nodeRoot;
    this.output = "global _start\n_start:\n";

    this.variables = new HashMap<String, Integer>();
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
      Object variableLocation = variables.get(((NodeIdentifier) nodeExpression.object()).identifier().value());

      if (variableLocation == null) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      push("QWORD [rsp + " + ((stackSize - ((Integer) variableLocation) - 1) * 8) + "]");
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

      if (variables.containsKey(nodeStatementSet.identifier().value())) {
        System.out.println("generation: variable already declared");
        System.exit(1);
      }

      variables.put(nodeStatementSet.identifier().value(), stackSize);
      generateExpression(nodeStatementSet.expression());
    } else if (nodeStatement.object().getClass() == NodeStatementAssignment.class) {
      NodeStatementAssignment nodeStatementAssignment = (NodeStatementAssignment) nodeStatement.object();

      if (!variables.containsKey(nodeStatementAssignment.identifier().value())) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      variables.put(nodeStatementAssignment.identifier().value(), stackSize);
      generateExpression(nodeStatementAssignment.expression());
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
