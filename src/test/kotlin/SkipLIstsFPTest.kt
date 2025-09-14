package valkey.kotlin.skiplists.fp

/**
 * Functor:
 *   Let C and D be categories. A functor F from C to D is a mapping that:
 *   - associates each object X in C to an object F(X) in D.
 *   - associates each morphisam f: X -> Y in C to a morphism F(f): F(X) -> F(Y) in D such that the following two conditions hold:
 *     - F(id_X) = id_F(X) for every object X in C
 *     - F(g * f) = F(g) * F(f) for all morphisms f: X -> Y and g: Y -> Z in C.
 *   That is, functors must preserve identity morphisms and composition of morphisms.
 *   In functional programming, a functor is a design pattern inspired by the definition from category theory
 *   that allows one to apply a function to values inside a generic type without changing the structure of the generic type.
 *
 * Monad:
 *   In functional programming, monads are a way to structure computations as a sequence of steps,
 *   where each step not only produces a value but also some extra information about the computation,
 *   such as a potential failure, non-determinism, or side effect.
 *   More formally, a monad is a type constructor M equipped with two operations,
 *   - return : <A>(a : A) -> M(A) which lifts a value into the monadic context
 *   - Bind : <A,B>(m_a : M(A), f : A -> M(B)) -> M(B) which chains monadic computations.
 *            In simpler terms, monads can be thought of as interfaces implemented on type constructors,
 *            that allow for functions to abstract over various type constructor variants that implement monad.
 */

interface Monoid<T> {
    // An identity element
    val empty: T
    // Associative binary operation
    fun T.combine(other: T): T
}

class ListMonoid<T> : Monoid<List<T>> {
    override val empty: List<T> = emptyList()
    override fun List<T>.combine(other: List<T>) = this + other
}

data class Node<K : Comparable<K>, V>(
    val key: K?,
    val value: V?,
    val forwards: List<Node<K, V>?>
) {
    val levelCount: Int get() = forwards.size
}

data class SkipList<K : Comparable<K>, V>(
    val head: Node<K, V>,
    val level: Int,
    val maxLevel: Int = 16,
    val p: Double = 0.5
) {
    // TODO
}

class SkipListsFPTest {
}