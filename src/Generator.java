import types.Parsing.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Iterator;

public class Generator {
  private Root root;
  private String output;

  private int currLabel;
  private int stackSize;
  //          name  , stack location
  private Map<String, Integer> variables;
  //            initial variables
  private Stack<Integer> scopes;

  public Generator(Root root) {
    this.root = root;
    this.output = "section .text\n    global _start\n_start:\n";

    this.variables = new LinkedHashMap<String, Integer>();
    this.scopes = new Stack<Integer>();
  }


  private void generateTerm(Term term) {
    if (term.object() instanceof IntegerLiteral integerLiteral) {
      output += "    mov rax, " + integerLiteral.integer().value() + "\n";
      push("rax");
    } else if (term.object() instanceof BooleanLiteral booleanLiteral) {
      output += "    mov rax, " + ((booleanLiteral.bool().value() == "true") ? "1" : "0") + "\n";
      push("rax");
    } else if (term.object() instanceof Identifier identifier) {
      Object variableLocation = variables.get(identifier.identifier().value());

      if (variableLocation == null) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      push("QWORD [rsp + " + ((stackSize - ((Integer) variableLocation)) * 8) + "]");
    } else if (term.object() instanceof Parentheses parentheses) {
      generateExpression(parentheses.expression());
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
    else {
      if (expressionBinary.type() == ExpressionBinaryType.and || expressionBinary.type() == ExpressionBinaryType.or) {
        if (expressionBinary.type() == ExpressionBinaryType.and) output += "    and rax, rbx\n";
        else output += "    or rax, rbx\n";

        output += "    cmp rax, 1\n";
        output += "    je l" + currLabel + "True\n";
      } else {
        output += "    cmp rax, rbx\n";

        if (expressionBinary.type() == ExpressionBinaryType.equal) output += "    je l" + currLabel + "True\n";
        else if (expressionBinary.type() == ExpressionBinaryType.notEqual) output += "    jne l" + currLabel + "True\n";
        else if (expressionBinary.type() == ExpressionBinaryType.less) output += "    jl l" + currLabel + "True\n";
        else if (expressionBinary.type() == ExpressionBinaryType.lessEqual) output += "    jle l" + currLabel + "True\n";
        else if (expressionBinary.type() == ExpressionBinaryType.greater) output += "    jg l" + currLabel + "True\n";
        else if (expressionBinary.type() == ExpressionBinaryType.greaterEqual) output += "    jge l" + currLabel + "True\n";
      }

      output += "    mov rax, 0\n";
      output += "    jmp l" + currLabel + "End\n";

      output += "l" + currLabel + "True:\n";
      output += "    mov rax, 1\n";
      
      output += "l" + currLabel + "End:\n";

      currLabel++;
    }

    push("rax");
  }

  private void generateExpression(Expression expression) {
    if (expression.object() instanceof Term term) {
      generateTerm(term);
    } else if (expression.object() instanceof ExpressionBinary expressionBinary) {
      generateExpressionBinary(expressionBinary);
    }
  }

  private void generateStatement(Statement statement) {
    if (statement.object() instanceof Scope scope) {
      scopes.push(variables.size());

      for (Statement childStatement : scope.statements()) generateStatement(childStatement);

      int popCount = variables.size() - scopes.lastElement();

      output += "    add rsp, " + popCount * 8 + "\n";
      stackSize -= popCount;

      Iterator<Entry<String, Integer>> iterator = variables.entrySet().iterator();
      for (int i = 0; i < scopes.lastElement(); i++) iterator.next();
      for (int i = 0; i < popCount; i++) variables.remove(iterator.next().getKey());

      scopes.pop();
    } else if (statement.object() instanceof StatementStop statementStop) {
      generateExpression(statementStop.expression());
      output += "    mov rax, 60\n";
      pop("rdi");
      output += "    syscall\n";
    } else if (statement.object() instanceof StatementSet statementSet) {
      if (variables.containsKey(statementSet.identifier().value())) {
        System.out.println("generation: variable already declared");
        System.exit(1);
      }

      if (statementSet.expression() == null) {
        output += "    mov rax, 0\n";
        push("rax");
      } else generateExpression(statementSet.expression());

      variables.put(statementSet.identifier().value(), stackSize);
    } else if (statement.object() instanceof StatementAssignment statementAssignment) {
      if (!variables.containsKey(statementAssignment.identifier().value())) {
        System.out.println("generation: undeclared variable");
        System.exit(1);
      }

      generateExpression(statementAssignment.expression());
      variables.put(statementAssignment.identifier().value(), stackSize);
    } else if (statement.object() instanceof StatementCondition statementCondition) {
      generateExpression(statementCondition.expression());

      int label = currLabel;
      currLabel++;

      pop("rax");

      output += "    cmp rax, 1\n";
      output += "    je l" + label + "True\n";
      output += "    jmp l" + label + "End\n";

      output += "l" + label + "True:\n";
      generateStatement(statementCondition.statement());

      output += "l" + label + "End:\n";
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
