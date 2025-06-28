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
