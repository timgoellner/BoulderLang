import types.Lexing.Token;
import types.Parsing.*;
import types.Generating.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Arrays;

public class Generator {
  private Root root;
  private String output;

  private int currLabel;
  private int stackSize;
  //          name
  private Map<String, Variable> variables;
  //          name  , parameterCount
  private Map<String, int> methods;
  private Stack<Variable> recentTerms;
  private Token recentToken;
  //            initial variables
  private Stack<Integer> scopes;

  public Generator(Root root) {
    this.root = root;
    this.output = "section .text\n    global _start\n_start:\n";

    this.variables = new LinkedHashMap<String, Variable>();
    this.methods = new HashMap<String, int>();
    this.recentTerms = new Stack<Variable>();
    this.recentToken = null;
    this.scopes = new Stack<Integer>();
  }


  private void generateTerm(Term term) {
    if (term.object() instanceof IntegerLiteral integerLiteral) {
      recentToken = integerLiteral.integer();

      if (Double.parseDouble(recentToken.value()) > 4294967295d) generateError("generation: integer literal out of range 2^32-1");

      push(recentToken.value(), true);

      recentTerms.push(new Variable(VariableType.integer, stackSize, 1));
    } else if (term.object() instanceof StringLiteral stringLiteral) {
      recentToken = stringLiteral.string();

      char[] characters = recentToken.value().toCharArray();

      push("0", true);

      for (int i = characters.length-1; i >= 0; i--) {
        push(Integer.toString((int) characters[i]), true);
      }

      Variable variable = new Variable(VariableType.string, stackSize, characters.length);
      recentTerms.push(variable);
    } else if (term.object() instanceof BooleanLiteral booleanLiteral) {
      recentToken = booleanLiteral.bool();

      push(((recentToken.value() == "true") ? "1" : "0"), true);

      recentTerms.push(new Variable(VariableType.bool, stackSize, 1));
    } else if (term.object() instanceof ArrayLiteral arrayLiteral) {
      Object lengthObject = arrayLiteral.length().object();

      if (!(lengthObject instanceof IntegerLiteral)) generateError("generation: needs integer, got " + lengthObject.getClass().getSimpleName());

      int arrayLength = Integer.parseInt(((IntegerLiteral) lengthObject).integer().value());

      output += "    sub rsp, " + String.valueOf(arrayLength*8) + "\n";
      stackSize += arrayLength;

      recentTerms.push(new Variable(VariableType.array, stackSize, arrayLength));  
    } else if (term.object() instanceof Identifier identifier) {
      recentToken = identifier.identifier();

      Object variableObj = variables.get(recentToken.value());
      if (variableObj == null) generateError("generation: undeclared variable '" + recentToken.value() + "'");

      Variable variable = (Variable) variableObj;
      if (variable.type() == VariableType.string) {
        push("0", true);
        
        for (int i = variable.length()-1; i >= 0; i--) {
          push("qword [rsp + " + ((stackSize - variable.stackLocation() + i) * 8) + "]", true);
        }
      } else {
        push("qword [rsp + " + ((stackSize - variable.stackLocation()) * 8) + "]", true);
      }

      recentTerms.push(variable.withStackLocation(stackSize));
    } else if (term.object() instanceof ArrayIdentifier arrayIdentifier) {
      recentToken = arrayIdentifier.identifier();

      Object variableObj = variables.get(recentToken.value());
      if (variableObj == null) generateError("generation: undeclared variable '" + recentToken.value() + "'");
      Variable variable = (Variable) variableObj;

      generateExpression(arrayIdentifier.offset());
      Variable offset = recentTerms.pop();
      if (offset.type() != VariableType.integer) generateError("generation: cannot convert from " + offset.type().name() + " to " + VariableType.integer.name());
      pop("rax", true);
      output += "    lea rax, [rax * 8]\n";

      push("qword [rsp + " + ((stackSize - variable.stackLocation()) * 8) + " + rax]", true);

      recentTerms.push(new Variable(VariableType.integer, stackSize, 1));
    } else if (term.object() instanceof Parentheses parentheses) {
      generateExpression(parentheses.expression());
    } else if (term.object() instanceof TermNegated termNegated) {
      generateTerm(termNegated.term());

      if (recentTerms.peek().type() != VariableType.bool) generateError("generation: not operator is undefined for type " + recentTerms.peek().type().name());

      output += "    xor qword [rsp], 1\n";
    }
  }

  private void generateExpressionBinary(ExpressionBinary expressionBinary) {
    generateExpression(expressionBinary.expressionRight());
    generateExpression(expressionBinary.expressionLeft());

    pop("rax", true);
    pop("rbx", true);

    Variable expressionLeft = recentTerms.pop();
    Variable expressionRight = recentTerms.pop();
    if (expressionLeft.type() != expressionRight.type()) generateError("generation: " + expressionBinary.type().name() + " operator is undefined for types " + expressionLeft.type().name() + " and " + expressionRight.type().name());

    VariableType variableType = expressionLeft.type();
    if (variableType == VariableType.string || variableType == VariableType.array) generateError("generation: " + expressionBinary.type().name() + " operator is undefined for type " + variableType.name());

    boolean mathType = true;
    switch (expressionBinary.type()) {
      case addition:
        output += "    add rax, rbx\n";
        break;
      case subtraction:
        output += "    sub rax, rbx\n";
        break;
      case multiplication:
        output += "    mul rbx\n";
        break;
      case division:
        output += "    div rbx\n";
        break;
      default:
        mathType = false;
        break;
    }

    if (mathType && variableType == VariableType.bool) generateError("generation: " + expressionBinary.type().name() + " operator is undefined for type " + variableType.name());

    if (!mathType) {
      if (expressionBinary.type() == ExpressionBinaryType.and || expressionBinary.type() == ExpressionBinaryType.or) {
        if (expressionBinary.type() == ExpressionBinaryType.and) output += "    and rax, rbx\n";
        else output += "    or rax, rbx\n";

        output += "    cmp rax, 1\n";
        output += "    je l" + currLabel + "True\n";
      } else {
        output += "    cmp rax, rbx\n";

        if (expressionBinary.type() == ExpressionBinaryType.equal) output += "    je l" + currLabel + "True\n";
        else if (expressionBinary.type() == ExpressionBinaryType.notEqual) output += "    jne l" + currLabel + "True\n";
        else {
          if (variableType == VariableType.bool) generateError("generation: " + expressionBinary.type().name() + " operator is undefined for type " + variableType.name());

          if (expressionBinary.type() == ExpressionBinaryType.less) output += "    jl l" + currLabel + "True\n";
          else if (expressionBinary.type() == ExpressionBinaryType.lessEqual) output += "    jle l" + currLabel + "True\n";
          else if (expressionBinary.type() == ExpressionBinaryType.greater) output += "    jg l" + currLabel + "True\n";
          else if (expressionBinary.type() == ExpressionBinaryType.greaterEqual) output += "    jge l" + currLabel + "True\n";
        }

        variableType = VariableType.bool;
      }

      output += "    mov rax, 0\n";
      output += "    jmp l" + currLabel + "End\n";

      output += "l" + currLabel + "True:\n";
      output += "    mov rax, 1\n";
      
      output += "l" + currLabel + "End:\n";

      currLabel++;
    }

    push("rax", true);
    recentTerms.push(new Variable(variableType, stackSize, 1));
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

      Object[] variableArray = (Object[]) variables.keySet().toArray();
      List<Object> popVariables = Arrays.asList(variableArray).subList(variableArray.length-popCount, variableArray.length);

      int dataPopCount = 0;
      for (Object popVariable : popVariables) {
        Variable variable = variables.get(popVariable);
        dataPopCount += variable.length();

        if (variable.type() == VariableType.string) dataPopCount ++;
      }

      output += "    add rsp, " + dataPopCount * 8 + "\n";
      stackSize -= dataPopCount;

      if (popCount > 0) variables.keySet().removeAll(popVariables);

      scopes.pop();
    } else if (statement.object() instanceof StatementStop statementStop) {
      generateExpression(statementStop.expression());
      if (recentTerms.pop().type() == VariableType.string) generateError("generation: stop subroutine not defined for type " + VariableType.string.name());

      output += "    mov rax, 60\n";
      pop("rdi", true);
      output += "    syscall\n";
    } else if (statement.object() instanceof StatementSet statementSet) {
      recentToken = statementSet.identifier();

      if (variables.containsKey(statementSet.identifier().value())) generateError("generation: variable already declared '" + statementSet.identifier().value() + "'");

      if (statementSet.value() == null) {
        push("0", true);
        recentTerms.push(new Variable(VariableType.integer, stackSize, 1));
      } else generateExpression(statementSet.value());

      variables.put(statementSet.identifier().value(), recentTerms.pop());
    } else if (statement.object() instanceof StatementAssignment statementAssignment) {
      Object identifierObject = statementAssignment.identifier().object();

      if (identifierObject instanceof Identifier identifier) {
        recentToken = identifier.identifier();

        if (!variables.containsKey(recentToken.value())) generateError("generation: undeclared variable '" + recentToken.value() + "'");
        Variable variable = variables.get(identifier.identifier().value());

        generateExpression(statementAssignment.value());
        Variable value = recentTerms.pop();
        if (variable.type() != value.type()) generateError("generation: cannot convert from " + value.type().name() + " to " + variable.type().name());
        if (variable.type() == VariableType.string || variable.type() == VariableType.array) generateError("generation: cannot reassign " + variable.type().name());

        int shiftSize = (stackSize - variable.stackLocation() - 1) * 8;
        pop("rax", true);
        output += "    mov [rsp + " + shiftSize + "], rax\n";
      } else if (identifierObject instanceof ArrayIdentifier arrayIdentifier) {
        recentToken = arrayIdentifier.identifier();

        if (!variables.containsKey(recentToken.value())) generateError("generation: undeclared variable '" + recentToken.value() + "'");
        Variable variable = variables.get(arrayIdentifier.identifier().value());

        generateExpression(arrayIdentifier.offset());
        Variable offset = recentTerms.pop();
        if (offset.type() != VariableType.integer) generateError("generation: cannot convert from " + offset.type().name() + " to " + VariableType.integer.name());
        pop("rax", true);
        output += "    lea rcx, [rax * 8]\n";
        
        generateExpression(statementAssignment.value());
        Variable value = recentTerms.pop();
        if (value.type() != VariableType.integer) generateError("generation: cannot convert from " + value.type().name() + " to " + VariableType.integer.name()); 
      
        int shiftSize = (stackSize - variable.stackLocation() - 1) * 8;
        pop("rax", true);
        output += "    mov [rsp + " + shiftSize + " + rcx], rax\n";
        output += "    xor rcx, rcx\n";
      }
    } else if (statement.object() instanceof StatementPrint statementPrint) {
      generateExpression(statementPrint.expression());

      Variable recentTerm = recentTerms.pop();

      if (recentTerm.type() == VariableType.array) generateError("generation: cannot print array");

      if (recentTerm.type() == VariableType.string) {
        output += "    mov rsi, rsp\n";
        output += "    mov rax, 1\n";
        output += "    mov edi, 1\n";
        output += "    mov rdx, " + recentTerm.length()*8 + "\n";
        output += "    syscall\n";

        output += "    push 10\n";
        output += "    mov rsi, rsp\n";
        output += "    mov rax, 1\n";
        output += "    mov edi, 1\n";
        output += "    mov rdx, 1\n";
        output += "    syscall\n";

        output += "    add rsp, " + (recentTerm.length()+2)*8 + "\n";
        stackSize -= recentTerm.length()+1;

        output += "    xor rdx, rdx\n";
      } else {
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
      }

      currLabel++;
    } else if (statement.object() instanceof Branch branch) {
      generateExpression(branch.condition());

      Variable condition = recentTerms.pop();
      if (condition.type() != VariableType.bool) generateError("generation: cannot convert from " + condition.type().name() + " to " + VariableType.bool.name());

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
      recentTerms.pop();

      pop("rax", true);

      output += "    cmp rax, 1\n";
      output += "    jne l" + label + "End\n";

      generateStatement(loop.statement());
      output += "    jmp l" + label + "Start\n";

      output += "l" + label + "End:\n";
    } else if (statement.object() instanceof Method method) {
      String labelName = method.identifier().identifier().value()

      methods.put(labelName, method.parameters().size());

      output += "    jmp " + labelName + "End\n";
      output += labelName  + "Start:\n";

      generateStatement(method.statement());

      output += labelName + "End:\n";
    } else if (statement.object() instanceof StatementCall statementCall) {
      String callName = statementCall.identifier().identifier().value();
      int callParameters = statementCall.parameters().size();
      Integer parameters = methods.get(methodName);

      if (parameters == null) generateError("generation: undeclared method '" + callName + "'");
      if (parameters.intValue() != callParameters) generateError("generation: method is defined for " + parameters.intValue() + " parameters, but got " + callParameters);
      
      int index = 0;
      for (Expression expression : statementCall.parameters()) {
        generateExpression(expression);
        Variable parameter = recentTerms.pop();
        if (parameter.type() != VariableType.integer) gernerateError("generation: cannot convert from " + parameter.type().name() + " to " + VariableType.integer.name());
        
        variables.put(callName + index++, parameter);
      }


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


  private void push(String value, boolean incStackSize) {
    output += "    push " + value + "\n";
    if (incStackSize) stackSize++;
  }

  private void pop(String register, boolean decStackSize) {
    output += "    pop " + register + "\n";
    if (decStackSize) stackSize--;
  }

  private void generateError(String msg) {
    System.out.println(recentToken.row() + ":" + recentToken.column() + ": ERROR: " + msg);
    System.exit(1);
  }
}
