# Sealed classes and interfaces

Sealed classes and interfaces provide controlled inheritance of your class hierarchies.

All direct subclasses of a sealed class are known at compile time.

No other subclasses may appear outside the module and package within which the sealed class is defined.

Once a module with a sealed class or interface is compiled, no new implementations can be created.

Best used scenarios:
- Limited class inheritance is desired
- Type-safe design is required
- Working with closed APIs
