package study

import kotlinx.coroutines.*
import kotlin.test.Test
import kotlin.test.assertEquals

typealias Par<A> = (CoroutineScope) -> Deferred<A>
fun <A> unit(a: A): Par<A> = { CompletableDeferred(a) }

fun <A> List<A>.splitAt(pivot: Int): Pair<List<A>, List<A>> =
    this.subList(0, pivot) to this.subList(pivot, this.size)

class FPTest {
    interface Category<T> {
        fun identity(x: T): (T) -> T
        fun <A, B, C> compose(f: (A) -> B, g: (B) -> C): (A) -> C
    }

    object StringCategory : Category<String> {
        override fun identity(x: String): (String) -> String = { x }
        override fun <A, B, C> compose(f: (A) -> B, g: (B) -> C): (A) -> C = { a: A -> g(f(a)) }
    }

    interface Monoid<A> {
        fun combine(a1: A, a2: A): A
        val nil: A
    }

    object StringMonoid : Monoid<String> {
        override fun combine(a1: String, a2: String): String = a1 + a2
        override val nil: String = ""
    }

    object IntAddition : Monoid<Int> {
        override fun combine(a1: Int, a2: Int): Int = a1 + a2
        override val nil: Int = 0
    }

    object BooleanAnd : Monoid<Boolean> {
        override fun combine(a1: Boolean, a2: Boolean): Boolean = a1 && a2
        override val nil: Boolean = true
    }

    fun <A, B, C> map2(pa: Par<A>, pb: Par<B>, f: (A, B) -> C): Par<C> = { scope ->
        val da = pa(scope)
        val db = pb(scope)
        scope.async { f(da.await(), db.await()) }
    }

    fun <B> parMonoid(m: Monoid<B>): Monoid<Par<B>> = object : Monoid<Par<B>> {
        override fun combine(a: Par<B>, b: Par<B>): Par<B> =
            map2(a, b) { x, y -> m.combine(x, y) }
        override val nil: Par<B> = unit(m.nil)
    }

    @Test
    fun monoid() {
        val f = { s: String -> s.uppercase() }
        val g = { s: String -> "$s!" }
        val h = { s: String -> s.repeat(2) }

        val left = StringCategory.compose(StringCategory.compose(f, g), h)
        val right = StringCategory.compose(f, StringCategory.compose(g, h))

        assertEquals(left("hello"), right("hello"))

        val words = listOf("Hic", "Est", "Index")
        val rightResults = words.foldRight(StringMonoid.nil, StringMonoid::combine)
        val leftResults = words.fold(StringMonoid.nil, StringMonoid::combine)
        assertEquals(rightResults, leftResults)

        fun <A, B> foldMap(la: List<A>, m: Monoid<B>, f: (A) -> B): B =
            la.fold(m.nil) { b: B, a: A -> m.combine(b, f(a)) }

        val results = foldMap(words, StringMonoid) { "$it!" }
        println(results)

        fun <A, B> parFoldMap(
            lst: List<A>,
            pm: Monoid<Par<B>>,
            f: (A) -> B
        ): Par<B> =
            when {
                lst.size >= 2 -> {
                    val (la1, la2) = lst.splitAt(lst.size / 2)
                    pm.combine(parFoldMap(la1, pm, f), parFoldMap(la2, pm, f))
                }
                lst.size == 1 -> unit(f(lst.first()))
                else -> pm.nil
            }

        suspend fun <A> run(pa: Par<A>): A = coroutineScope { pa(this).await() }
        fun <A> runBlockingPar(pa: Par<A>): A = runBlocking { run(pa) }

        val parResults = runBlockingPar(parFoldMap(words, parMonoid(StringMonoid)) { "$it!" })
        assertEquals(results, parResults)
    }
}
