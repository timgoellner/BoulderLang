import types.Parsing.*;

import java.util.HashMap;
import java.util.Map;

public class Generator {
  private Root root;
  private String output;

  private int stackSize;
  //          name  , stackLocation
  private Map<String, Integer> variables;

  public Generator(Root root) {
    this.root = root;
    this.output = "global _start\n_start:\n";

    this.variables = new HashMap<String, Integer>();
  }


  private void generateTerm(Term term) {
    if (term.object().getClass() == IntegerLiteral.class) {
      output += "    mov rax, " + ((IntegerLiteral) term.object()).integer().value() + "\n";
      push("rax");
    } else if (term.object().getClass() == Identifier.class) {
      Object variableLocation = variables.get(((Identifier) term.object()).identifier().value());

      if (variableLocation == null) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      push("QWORD [rsp + " + ((stackSize - ((Integer) variableLocation)) * 8) + "]");
    } else if (term.object().getClass() == Parentheses.class) {
      generateExpression(((Parentheses) term.object()).expression());
    }
  }

  private void generateExpressionBinary(ExpressionBinary expressionBinary) {
    generateExpression(expressionBinary.expressionRight());
    generateExpression(expressionBinary.expressionLeft());

    pop("rax");
    pop("rbx");

    if (expressionBinary.type() == ExpressionBinaryType.addition) output += "    add rax, rbx\n";
    else if (expressionBinary.type() == ExpressionBinaryType.subtraction) output += "    sub rax, rbx\n";
    else if (expressionBinary.type() == ExpressionBinaryType.multiplication) output += "    mul rbx\n";
    else if (expressionBinary.type() == ExpressionBinaryType.division) output += "    div rbx\n";

    push("rax");
  }

  private void generateExpression(Expression expression) {
    if (expression.object().getClass() == Term.class) {
      generateTerm((Term) expression.object());
    } else if (expression.object().getClass() == ExpressionBinary.class) {
      generateExpressionBinary((ExpressionBinary) expression.object());
    }
  }

  private void generateStatement(Statement statement) {
    if (statement.object().getClass() == StatementStop.class) {
      generateExpression(((StatementStop) statement.object()).expression());
      output += "    mov rax, 60\n";
      pop("rdi");
      output += "    syscall\n";
    } else if (statement.object().getClass() == StatementSet.class) {
      StatementSet statementSet = (StatementSet) statement.object();

      if (variables.containsKey(statementSet.identifier().value())) {
        System.out.println("generation: variable already declared");
        System.exit(1);
      }

      generateExpression(statementSet.expression());
      variables.put(statementSet.identifier().value(), stackSize);
    } else if (statement.object().getClass() == StatementAssignment.class) {
      StatementAssignment statementAssignment = (StatementAssignment) statement.object();

      if (!variables.containsKey(statementAssignment.identifier().value())) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      generateExpression(statementAssignment.expression());
      variables.put(statementAssignment.identifier().value(), stackSize);
    }
  }

  public String generate() {
    for (Statement statement : root.statements()) {
      generateStatement(statement);
    }

    output += "    mov rax, 60\n";
    output += "    mov rdi, 0\n";
    output += "    syscall\n";

    return output;
  }


  private void push(String register) {
    output += "    push " + register + "\n";
    stackSize++;
  }

  private void pop(String register) {
    output += "    pop " + register + "\n";
    stackSize--;
  }
}
