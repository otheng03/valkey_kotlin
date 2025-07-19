# Kotlin basics

## The difference between expressions and statements

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

## Making function definitions more concise by using expression bodies

If a function is written with its body in curly braces, we say that this function has a block body.
If it returns an expression directly, it has an expression body.

```Kotlin
fun max(a: Int, b: Int): Int = if (a > b) a else b
```

## Marking a variable as read only or reassignable

- val (from value) declares a read-only reference. A variable declared with val can be assigned only once.
After it has been initialized, it can’t be reassigned a different value.
(For comparison, in Java, this would be expressed via the final modifier.)
- var (from variable) declares a reassignable reference. You can assign other values to such a variable,
even after it has been initialized. (This behavior is analogous to a regular, non-final variable in Java.)

**Favor functional style**

By default, you should strive to declare all variables in Kotlin with the val keyword;
change it to var only if necessary. Using read-only references,
immutable objects, and functions without side effects allows you to take advantage of the benefits offered
by the functional programming style.


## Kotlin source code layout: Directories and packages

In Kotlin, you can put multiple classes in the same file and choose any name for that file.
Kotlin also doesn’t impose any restrictions on the layout of source files on disk;
you can use any directory structure to organize your files.
For instance, you can define all the content of the package geometry.shapes in the file shapes.kt
and place this file in the geometry folder without creating a separate shapes folder (see figure 2.4).

```
geometry
├── example.kt → geometry.example package
└── shapes.kt → geometry.shapes package
```

In most cases, however, it’s still a good practice to organize source files into directories according to the package structure,
following Java’s directory layout. Sticking to that structure is especially important in projects where Kotlin is mixed
with Java because doing so allows you to migrate the code gradually without introducing any surprises.
But you shouldn’t hesitate to pull multiple classes into the same file, especially if the classes are small.
