import types.Lexing.Token;
import types.Parsing.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Arrays;

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
      if (Double.parseDouble(integerLiteral.integer().value()) > 4294967295d) generateError(integerLiteral.integer(), "generation: number literal out of range 2^32-1");

      output += "    mov rax, " + integerLiteral.integer().value() + "\n";
      push("rax", true);
    } else if (term.object() instanceof BooleanLiteral booleanLiteral) {
      output += "    mov rax, " + ((booleanLiteral.bool().value() == "true") ? "1" : "0") + "\n";
      push("rax", true);
    } else if (term.object() instanceof Identifier identifier) {
      Object variableLocation = variables.get(identifier.identifier().value());
      
      if (variableLocation == null) generateError(identifier.identifier(), "generation: undeclared variable '" + identifier.identifier().value() + "'");

      push("QWORD [rsp + " + ((stackSize - ((Integer) variableLocation)) * 8) + "]", true);
    } else if (term.object() instanceof Parentheses parentheses) {
      generateExpression(parentheses.expression());
    } else if (term.object() instanceof TermNegated termNegated) {
      generateTerm(termNegated.term());

      pop("rax", true);

      output += "    cmp rax, 0\n";
      output += "    je l" + currLabel + "True\n";
      output += "    mov rax, 0\n";
      output += "    jmp l" + currLabel + "End\n";

      output += "l" + currLabel + "True:\n";
      output += "    mov rax, 1\n";

      output += "l" + currLabel + "End:\n";
      push("rax", true);

      currLabel++;
    }
  }

  private void generateExpressionBinary(ExpressionBinary expressionBinary) {
    generateExpression(expressionBinary.expressionRight());
    generateExpression(expressionBinary.expressionLeft());

    pop("rax", true);
    pop("rbx", true);

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

    push("rax", true);
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

      for (Statement childStatement : scope.statements()) {
        output += "    ; " + childStatement.object().getClass().getSimpleName() + "\n";
        generateStatement(childStatement);
      }

      int popCount = variables.size() - scopes.lastElement();

      output += "    add rsp, " + popCount * 8 + "\n";
      stackSize -= popCount;

      Object[] variableArray = variables.keySet().toArray();
      if (popCount > 0) variables.keySet().removeAll(Arrays.asList(variableArray).subList(variableArray.length-popCount, variableArray.length-1));

      scopes.pop();
    } else if (statement.object() instanceof StatementStop statementStop) {
      generateExpression(statementStop.expression());
      output += "    mov rax, 60\n";
      pop("rdi", true);
      output += "    syscall\n";
    } else if (statement.object() instanceof StatementSet statementSet) {
      if (variables.containsKey(statementSet.identifier().value())) generateError(statementSet.identifier(), "generation: variable already declared '" + statementSet.identifier().value() + "'");

      if (statementSet.expression() == null) {
        output += "    mov rax, 0\n";
        push("rax", true);
      } else generateExpression(statementSet.expression());

      variables.put(statementSet.identifier().value(), stackSize);
    } else if (statement.object() instanceof StatementAssignment statementAssignment) {
      if (!variables.containsKey(statementAssignment.identifier().value())) generateError(statementAssignment.identifier(), "generation: undeclared variable '" + statementAssignment.identifier().value() + "'");

      generateExpression(statementAssignment.expression());

      int shiftSize = (stackSize - variables.get(statementAssignment.identifier().value())) * 8;
      pop("rax", true);
      output += "    add rsp, " + shiftSize + "\n";
      push("rax", false);
      output += "    sub rsp, " + (shiftSize - 8) + "\n";
    } else if (statement.object() instanceof StatementPrint statementPrint) {
      generateExpression(statementPrint.expression());

      output += "    mov ebx, 10\n";
      pop("rax", true);

      output += "    push 0\n";
      output += "    push 10\n";

      output += "    cmp rax, 0\n";
      output += "    jns l" + currLabel + "Convert\n";
      output += "    mov rcx, 1\n";
      output += "    neg rax\n";

      output += "l" + currLabel + "Convert:\n";
      output += "    div ebx\n";

      output += "    add edx, 48\n";
      push("rdx", true);

      output += "    xor edx, edx\n";
      output += "    cmp eax, 0\n";
      output += "    jnz l" + currLabel + "Convert\n";

      output += "    cmp rcx, 1\n";
      output += "    jne l" + currLabel + "Print\n";
      output += "    xor rcx, rcx\n";
      output += "    push 45\n";

      output += "l" + currLabel + "Print:\n";

      output += "    mov rsi, rsp\n";
      output += "    mov rax, 1\n";
      output += "    mov edi, 1\n";
      output += "    mov rdx, 1\n";
      output += "    syscall\n";

      pop("rdx", true);
      output += "    cmp dx, 0\n";
      output += "    jnz l" + currLabel + "Print\n";

      currLabel++;
    } else if (statement.object() instanceof Branch branch) {
      generateExpression(branch.condition());

      int label = currLabel;
      currLabel++;

      pop("rax", true);

      output += "    cmp rax, 1\n";
      output += "    je l" + label + "True\n";
      output += "    jmp l" + label + "False\n";

      output += "l" + label + "True:\n";
      generateStatement(branch.statement());
      output += "    jmp l" + label + "End\n";

      output += "l" + label + "False:\n";
      if (branch.statementElse() != null) generateStatement(branch.statementElse());

      output += "l" + label + "End:\n";
    } else if (statement.object() instanceof Loop loop) {
      int label = currLabel;
      currLabel++;

      output += "l" + label + "Start:\n";
      generateExpression(loop.condition());

      pop("rax", true);

      output += "    cmp rax, 1\n";
      output += "    jne l" + label + "End\n";

      generateStatement(loop.statement());
      output += "    jmp l" + label + "Start\n";

      output += "l" + label + "End:\n";
    }
  }

  public String generate() {
    for (Statement statement : root.statements()) {
      output += "    ; " + statement.object().getClass().getSimpleName() + "\n";
      generateStatement(statement);
    }

    output += "    mov rax, 60\n";
    output += "    mov rdi, 0\n";
    output += "    syscall\n";

    return output;
  }


  private void push(String register, boolean incStackSize) {
    output += "    push " + register + "\n";
    if (incStackSize) stackSize++;
  }

  private void pop(String register, boolean decStackSize) {
    output += "    pop " + register + "\n";
    if (decStackSize) stackSize--;
  }

  private void generateError(Token token, String msg) {
    System.out.println(token.row() + ":" + token.column() + ": ERROR: " + msg);
    System.exit(1);
  }
}
