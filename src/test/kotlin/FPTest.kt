package study

import kotlinx.coroutines.*
import study.FPTest.Either
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

typealias Par<A> = (CoroutineScope) -> Deferred<A>
fun <A> unit(a: A): Par<A> = { CompletableDeferred(a) }

fun <A> List<A>.splitAt(pivot: Int): Pair<List<A>, List<A>> =
    this.subList(0, pivot) to this.subList(pivot, this.size)

interface AccountRepository {
    fun findBy(userId: UUID): Either<FPTest.AccountError.AccountNotFound, FPTest.Account2>
    fun save(account: FPTest.Account2)
}

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
        fun combine(a1: A, a2: A): A    // Associative binary operation
        val nil: A                      // An identity element
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

    /**
     * What is a monad?
     * A monad is a functional design pattern that solves recurrent problems such as:
     * - Nullability: Maybe/Option monad
     * - Error Handling: Either monad
     * - DI (Dependency Injection): Reader monad
     * - Logging: Writer monad
     * - Side Effects: State monad
     * - Collections: List monad
     * - Many others ...
     * A monad is a type in a context. Monads work with a type in a context, where context is a generic container that holds a value.
     * - Is a type that wraps another type/s.
     * - Is parameterised
     * - The context matters, is semantic, gives some form of quality to the underlying type.
     */

    sealed class Either<out A, out B> {
        data class Left<A>(val value: A) : Either<A, Nothing>()
        data class Right<B>(val value: B) : Either<Nothing, B>()

        fun <C> map(fn: (B) -> C): Either<A, C> = when (this) {
            is Right -> Right(fn(this.value))
            is Left -> this
        }

        fun <A, C> flatMap(fn: (B) -> Either<A, C>): Either<A, C> = when (this) {
            is Right -> fn(this.value)
            is Left -> this as Either<A, C>
        }
    }

    object NegativeAmount

    data class Account constructor(val balance: BigDecimal) {
        companion object {
            fun create(initialBalance: BigDecimal): Either<NegativeAmount, Account> =
                if (initialBalance < 0.toBigDecimal()) Either.Left(NegativeAmount)
                else Either.Right(Account(initialBalance))
        }

        fun deposit(amount: BigDecimal): Account = this.copy(balance = this.balance + amount)
    }

    sealed class AccountError {
        object NegativeAmount: AccountError()
        object NotEnoughFunds: AccountError()
        object AccountNotFound: AccountError()
    }

    data class Account2 constructor(val balance: BigDecimal) {
        companion object {
            private fun applyAmount(amount: BigDecimal, fn: (BigDecimal) -> Account2) =
                if (amount < ZERO) Either.Left(AccountError.NegativeAmount)
                else Either.Right(fn(amount))

            fun create(initialBalance: BigDecimal): Either<AccountError.NegativeAmount, Account2> =
                applyAmount(initialBalance) { Account2(it) }
        }

        fun deposit(amount: BigDecimal): Either<AccountError.NegativeAmount, Account2> =
            applyAmount(amount) { this.copy(balance = this.balance + it) }

        fun withdraw(amount: BigDecimal): Either<AccountError, Account2> =
            applyAmount(amount) { this.copy(balance = this.balance - it) }
                .flatMap {
                    if ((balance - amount) < ZERO) Either.Left(AccountError.NotEnoughFunds)
                    else Either.Right(Account2(balance - amount))
                }
    }

    @Test
    fun monad() {
        val account1 = Account.create(100.toBigDecimal())
        when (account1) {
            is Either.Right -> account1.value.deposit(100.toBigDecimal())
            is Either.Left -> TODO() // now what?
        }

        val account1_2 = Account.create(100.toBigDecimal())
            .map { a -> a.deposit(100.toBigDecimal()) }

        /**
         * A Monad is a couple of functions
         * Monads define two functions:
         * 1. Wrapping a value in a monad(the container), called return or unit
         *    A --(unit)--> A
         * 2. Also known as bind or flatmap, to apply a function to the contained value that outputs another monad.
         *    A --(fn: A -> B)--> B
         */
        val account2: Either<AccountError.NegativeAmount, Either<AccountError.NegativeAmount, Account2>> =
            Account2.create(100.toBigDecimal()).map { a -> a.deposit(100.toBigDecimal()) }

        val account2_2: Either<AccountError.NegativeAmount, Account2> = Account2.create(100.toBigDecimal())
            .flatMap { a -> a.deposit(100.toBigDecimal()) }
        println(account2_2)

        /**
         * A Monad is a Workflow/Pipeline builder
         * Monads allow you to compose small operations to achieve bigger purposes.
         * FP is all about make programs by composing functions.
         * So, this is what monads do, compose functions, chain functions, combine them to create workflows.
         */

        val account2_3 = Account2.create(100.toBigDecimal())
            .flatMap { a -> a.deposit(100.toBigDecimal()) }
            .flatMap { a -> a.withdraw(250.toBigDecimal()) }
        println("account2_3: $account2_3")

        // A service that transfers money between two different accounts
        class TransferMoney {
            operator fun invoke(debtor: Account2, creditor: Account2, amount: BigDecimal):
                    Either<AccountError, Pair<Account2, Account2>> =
                debtor.withdraw(amount)
                    .flatMap { d -> creditor.deposit(amount).map { Pair(d, it) } }
        }

        val debtor = Account2(200.toBigDecimal())
        val creditor = Account2(1000.toBigDecimal())
        val result = TransferMoney()(debtor, creditor, 100.toBigDecimal())
        println("result: $result")

        // Combine different actions, find an account, add some cash, and save it back with the new state
        class DepositCash(private val repository: AccountRepository) {
            operator fun invoke(userId: UUID, amount: BigDecimal): Either<AccountError, Unit> =
                repository.findBy(userId)
                    .flatMap { it.deposit(amount) }
                    .map(repository::save)
        }

        val userId = UUID.randomUUID()
        val repository = object : AccountRepository {
            var theOne = Account2(0.toBigDecimal())

            override fun findBy(userId: UUID): Either<AccountError.AccountNotFound, Account2> {
                return Either.Right(theOne)
            }

            override fun save(account: Account2) {
                theOne = account
            }

            override fun toString(): String = "theOne: $theOne"
        }
        DepositCash(repository)(userId, 100.toBigDecimal())
        println("repository: $repository")
    }

    @Test
    fun passingTrailinglambdas() {
        val items = listOf(1, 2, 3)

        /**
         * Passing trailing lambdas
         * According to Kotlin convention, if the last parameter of a function is a function,
         * then a lambda expression passed as the corresponding argument can be placed outside the parentheses:
         */
        val product = items.fold(1) { acc, e -> acc * e }
        assertEquals(6, product)

        /**
         * Such syntax is also known as trailing lambda.
         * If the lambda is the only argument in that call, the parentheses can be omitted entirely:
         */
        run { println("...") }

        /**
         * it: implicit name of a single parameter
         * It's very common for a lambda expression to have only one parameter.
         * If the compiler can parse the signature without any parameters,
         * the parameter does not need to be declared and -> can be omitted.
         * The parameter will be implicitly declared under the name it:
         */
        assertEquals(listOf(3), items.filter { it > 2 })
    }

}
