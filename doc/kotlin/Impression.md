# Omit type

```Kotlin
fun A(): ULong { ... }
fun B(): ULong { 
    val a = A()
}
```

If I don’t specify the type for a, it’s easier to change the return type of `A()` later.

This way, I won’t need to make as many changes if I decide to update the return type of `A()`.
