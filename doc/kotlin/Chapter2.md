
In Kotlin, if is an expression, not a statement.
The difference between an expression and a statement is that an expression has a value,
which can be used as part of another expression, whereas a statement is always a top-level
element in its enclosing block and doesn’t have its own value. In Kotlin, most control structures,
except the loops (for, while, and do/while), are expressions, which sets it apart from other languages,
like Java. Specifically, the ability to combine control structures with other expressions
lets you express many common patterns concisely, as you’ll see later in the book. As a sneak peek,
here are some snippets that are valid in Kotlin:

```Kotlin
val x = if (myBoolean) 3 else 5
val direction = when (inputString) {
   "u" -> UP
   "d" -> DOWN
   else -> UNKNOWN
}
val number = try {
   inputString.toInt()
} catch (nfe: NumberFormatException) {
   -1
}
```

On the other hand, Kotlin enforces assignments to always be statements—that is, 
when assigning a value to a variable, this assignment operation doesn’t itself return a value.

This helps avoid confusion between comparisons and assignments, 
which is a common source of mistakes in languages that treat them as expressions, 
such as Java or C/C++. That means the following isn’t valid Kotlin code:

```Kotlin
val number: Int
val alsoNumber = i = getNumber()
// ERROR: Assignments are not expressions,
// and only expressions are allowed in this context
```

If a function is written with its body in curly braces, we say that this function has a block body. 
If it returns an expression directly, it has an expression body.

```Kotlin
fun max(a: Int, b: Int): Int = if (a > b) a else b
```

- val (from value) declares a read-only reference. A variable declared with val can be assigned only once. 
After it has been initialized, it can’t be reassigned a different value. 
(For comparison, in Java, this would be expressed via the final modifier.)
- var (from variable) declares a reassignable reference. You can assign other values to such a variable, 
even after it has been initialized. (This behavior is analogous to a regular, non-final variable in Java.)

By default, you should strive to declare all variables in Kotlin with the val keyword; change it to var only if necessary. Using read-only references, 
immutable objects, and functions without side effects allows you to take advantage of the benefits offered by the functional programming style. 
