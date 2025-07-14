
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