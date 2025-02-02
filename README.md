# BoulderLang

Simle Compiler, which compiles to x86 64-bit nasm style assembly. <br>
**This language is a work in progress. Everything can change at any time, so use it at your own risk.**
<br>
## Usage
Compilation generates assembly code, compiles it with [nasm](https://nasm.us/) and links it with the [GNU linker](https://www.gnu.org/software/binutils/), <br>so make sure you have it in you `$PATH`.

```
$ cat main.boulder
print("Hello world!");
$ java -cp build Main main.boulder
$ ./main
Hello world!
```
<br>

## An Example

```
> number = 15;

(number > 20) stop(0);
\ (number > 10) {
  > code = 1;
  stop(code);
} \ stop(2);
```
This programm stops with `1`
<br><br>

## Language Reference
This is what the language supports so far.

### Integer Literal
A sequence of decimal digits up to 2^32-1.
```
327
```

### String Literal
A sequence of characters surrounded by double quotes.
```
"Hello world!"
```

### Boolean Literal
Either `true` or `false`, which are interpreted internally as `1` and `0`.
```
true
```

### Array Literal
An initializer for an array with its length, surrounded by square brackets.
*Note: An array can only contain integers*
```
[2]
```

### Identifier
A sequence of letters, decimal digits and/or underscores.
```
name
```

### Array Identifier
A Identifier of type array, suffixed with the array offset (index) in square brackets.
```
array[2]
```

### Parentheses
An *Expression* surrounded by parentheses.
```
(age)
```

### Negated Term
A *Term* prefixed with a dash (`!`), making it `true` if its `false` and `false` otherwise.
```
!false
```

### *Term (group class)*
Either *Integer Literal*, *String Literal*, *BooleanLiteral*, *Array Literal*, *Identifier*, *Array Identifier*, *Parentheses*, *Negated Term* or *Call*.
<br><br>

### Binary Expression Types

| Name          | Sign | Precedence |
| ------------- | --   | ---------- |
| multiplication| *    | 4          |
| division      | /    | 4          |
| addition      | +    | 3          |
| subtraction   | -    | 3          |
| equal         | ==   | 2          |
| notEqual      | !=   | 2          |
| less          | <    | 2          |
| lessEqual     | <=   | 2          |
| greater       | >    | 2          |
| greaterEqual  | >=   | 2          |
| and (bitwise) | &    | 1          |
| or (bitwise)  | \|   | 1          |

### Binary Expression
Two *Expressions* connected with a *Binary Expression Type*.
```
num + 23
```

### *Expression (group class)*
Either *Term* or *Binary Expression*.
<br><br>

### Branch
A branch executing a *Statement* when its condition *Expression* is `true`. Optionally executes a *Statement*-Else when the condition is `false` and the *Statement* is suffixed with `\`.
```
(number != -1) print(number);
\ print("please set a number");
```

### Loop
A loop that executes a *Statement* if its condition *Expression* is `true`, and then jumps back to its beginning, prefixed with `~`.
```
~(i > 0) {
  num = num*num;
  i = i - 1;
}
```

### Print Statement
A *Statement* printing an *Expression* to stdout suffixed with a line break.
```
print(34);
```

### Assignment Statement
A *Statement* setting an already initialised *Identifier* or *Array Identifier* equal to an *Expression*.
```
value = 76;
```

### Initialize Statement
A *Statement* setting a not already initialised *Identifier* equal to an *Expression*, prefixed with `>`.
```
> value = 76;
```

### Stop Statement
A *Statement* terminating the program with an exit code given by an *Expression*.
```
stop(32);
```

### Return Statement
A *Expression* prefixed with `->`, which sets the return value of a *Method* and returns from it.
```
-> a + b;
```

### Scope
*Statements* surrounded with curly braces (`{}`), having local variables, which cannot be accessed globally.
```
{
  > code = 1;
  print(code);
}
```

### Method
A *Identifier* prefixed with `°` and suffixed with more *Identifiers* surrounded with braces, which represent the parameters of the method. A *Method* contains one *Statement*, which will be executet using a *Call*. The method parameters and the return value are restricted to integers. The *Statement* of a *Method* cannot contain a *Initialize Statement* or another *Method*.
```
°sum(a, b) {
  -> a + b;
}
```

### Call
A *Identifier* suffixed with more *Identifiers* surrounded with braces, which represent the parameters of the method that is called.
```
sum(2, 3);
```

### *Statement (group class)*
Either *Initialize Statement*, *Assignment Statement*, *Stop Statement*, *Return Statement*, *Branch*, *Scope*, *Loop*, *Method* or *Call*.


### Comment
Everything after a `#` in a line is ignored by the compiler.
Multi-line comments start with a `#*` and end with a `*#`.
```
# This is a comment

#*
  This is a multi-
  line comment
*#
```
