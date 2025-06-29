# Sealed classes and interfaces

Sealed classes and interfaces provide controlled inheritance of your class hierarchies.

All direct subclasses of a sealed class are known at compile time.

No other subclasses may appear outside the module and package within which the sealed class is defined.

Once a module with a sealed class or interface is compiled, no new implementations can be created.

Best used scenarios:
- Limited class inheritance is desired
- Type-safe design is required
- Working with closed APIs

# Data class

It is primarily used to hold data.

For each data class, the compiler automatically generates additional member functions.

- equals()
- hashCode()
- toString() of the form "ClassName(val name=value)"
- compoentN() functions corresponding to the properties in their order of declaration.
- copy()

## Properties declared in the class body

The compiler only uses the properties defined inside the primary constructor for the automatically generated functions

To exclude a property from the generated implementations, declare it inside the class body.

```Kotlin
data class Person(val name: String) {
    var age: Int = 0
}

val person1 = Person("John")
person1.age = 10

val person2 = Person("John")
person2.age = 20

// person1 == person2: true
```

## Copying

Use the copy() function to copy an object, allowing you to alter some of its properties while keeping the reset unchaged.

```Kotlin
fun copy(name: String = this.name, age: Int = this.age) = User(name, age)

val jack = User(name = "Jack", age = 1)
val olderJack = jack.copy(age = 2)
```

## Data classes and destructuring declarations

Component functions generated for data classes make it possible to use them in destructuring declarations:

```Kotlin
val jane = User("Jane", 35)
val (name, age) = jane
println("$name, $age years of age")
// Jane, 35 years of age
```

# Operators and special symbols

- `?` marks a type as nullable.
- `?.` performs a safe call (calls a method or accesses a property if the receiver is non-nullable)
- `!!` asserts that an expression is non-nullable
- `===`, `!==` referential equality operators.

# BitField

kotlin-stdlib/kotlinx.cinterop.internal/CStruct/BitField
```Kotlin
@Target(allowedTargets = [AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER])
annotation class BitField(val offset: Long, val size: Int)
```

Properties
```Kotlin
val offset: Long
val size: Int
```

Operator

| Operation Name                | Java Operator | Kotlin `Int`\/`Long` Function |
|-------------------------------|---------------|-------------------------------|
| Conjunction (and)             | `a & b`       | `a and b`                     |
| Disjunction (or)              | `a \| b`      | `a or b`                      |
| Exclusive disjunction (xor)   | `a ^ b`       | `a xor b`                     |
| Inversion                     | `~a`          | `a.inv()`                     |
| Shift Left                    | `a << bits`   | `a shl bits`                  |
| Shift Right                   | `a >> bits`   | `a shr bits`                  |
| Unsigned Shift Right          | `a >>> bits`  | `a ushr bits`                 |

A brief history of discussion

- The Kotlin community has been arguing about bitwise operators for ten years.
  - Those who support the idea say that it’s a requirement of their low-level domains – usually, sound or video processing.
  - Those who oppose it put other things as more important and also point out that sometimes the code overcrowded with bitwise symbols is completely unreadable in languages that allow them.

# Function

## Infix notation

`infix` keyword enables a function can also be called using the infix notation(omitting the dot and parentheses for the call).
Infix function must meet the following requirements:
- Must be a member functions or extension functions
- Must have a single parameter
  - The parameter must not accept variable number arguments and must have no default value.

```Kotlin
infix fun Int.hasFlag(flag: Int): Boolean = this and flag != 0

if (flags hasFlag SETKEY_ALREADY_EXIST) print("true")
```

## Scope function

The Kotlin standard library contains several functions whose sole purpose is to execute a block of code within the context of an object.
When you call such a function on an object with a lambda expression provided,
it forms a temporary scope. In this scope, you can access the object without its name.
Such functions are called scope functions. There are five of them: let, run, with, apply, and also.

Basically, these functions all perform the same action: execute a block of code on an object.
What's different is how this object becomes available inside the block and what the result of the whole expression is.

### Function selection

| Function | Object reference | Return value     | Is extension function                          |
|----------|------------------|------------------|------------------------------------------------|
| `let`    | `it`             | Lambda result    | Yes                                            |
| `run`    | `this`           | Lambda result    | Yes                                            |
| `run`    | -                | Lambda result    | No: called without the context object          |
| `with`   | `this`           | Lambda result    | No: takes the context object as an argument    |
| `apply`  | `this`           | Context object   | Yes                                            |
| `also`   | `it`             | Context object   | Yes                                            |

- Executing a lambda on non-nullable objects: let
- Introducing an expression as a variable in local scope: let
- Object configuration: apply
- Object configuration and computing the result: run
- Running statements where an expression is required: non-extension run
- Additional effects: also
- Grouping function calls on an object: with

# Referential equality

Referential equality checks whether two objects are the exact same instance in memory.

```Kotlin
fun main() {
    var a = "Hello"
    var b = a
    var c = "world"
    var d = "world"

    println(a === b) // Prints whether a and b are referentially equal. The result is true.
    println(a === c) // false
    println(a !== c) // true
    println(c === d) // true
}
```

# Conventions

Directory structure

Unlike Java, Kotlin recommends to omit the common root package.
For example, if all the code in the project is in the org.example.kotlin package and its subpackages,
files with the org.example.kotlin package should be placed directly under the source root,
and files in org.example.kotlin.network.socket should be in the network/socket subdirectory of the source root.

```
/src/main/kotlin (org.example.kotlin)
/src/main/kotlin/network/socker (org.example.kotlin.network.socket)
```
