package study

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

interface Expr

class Num(val value: Int) : Expr
class Sum(val left: Expr, val right: Expr) : Expr

fun evalWithLogging(e: Expr): Int =
    when (e) {
        is Num -> {
            println("num: ${e.value}")
            e.value
        }
        is Sum -> {
            val left = evalWithLogging(e.left)
            val right = evalWithLogging(e.right)
            println("sum: $left + $right")
            left + right
        }
        else -> throw IllegalArgumentException("Unknown expression")
    }

fun readNumber(reader: BufferedReader) {
    val number = try {
        Integer.parseInt(reader.readLine())
    } catch (e: NumberFormatException) {
        return
    }

    println(number)
}

class KotlinStudyTest {
    @Test
    fun test() {
        evalWithLogging(Num(10))
        evalWithLogging(Sum(
            Sum(
                Num(1),Num(2)
            ), Num(4)))
    }

    @Test
    fun exception1() {
        val percentage = 1000
        if (percentage !in 0..100) {
            println("A percentage value must be between 0 and 100: $percentage")
        }
    }

    @Test
    fun exceptino2() {
        val reader = BufferedReader(StringReader("not a number"))
        readNumber(reader)
    }

    @Test
    fun extension() {
        open class View {
            open fun click() = println("View clicked")
            open fun showOff() = println("I'm a view!")
        }

        class Button: View() {
            override fun click() = println("Button clicked")
        }

        fun Button.showOff() = println("I'm a button!")

        val view: View = Button()
        view.showOff()
        // I'm a view!
    }

    private var zeroTime = System.currentTimeMillis()
    fun log(message: Any?) =
        println("${System.currentTimeMillis() - zeroTime} " +
                "[${Thread.currentThread().name}] $message")

    suspend fun doSomethingSlowly() {
        delay(500.milliseconds)
        println("I'm done")
    }

    @Test
    fun coroutine() {
        /**
         * A coroutine is an instance of a suspendable computation.
         * You can think of it as a block of code that can be executed concurrently (or even in parallel) with other
         * coroutines, Similar to a thread.
         * These coroutines contain the required machinery to suspend execution of the functions called in their body.
         * To create such a coroutine, you use one of the coroutine builder functions. There are a number of functions available.
         * - `runBlocking` is designed for bridging the world of blocking code and suspending functions.
         * - `launch` is used for starting new coroutines that don't return any values.
         *   - It is typically used for "start-and-forget" scenarios.
         * - `async` is for computing values in an asynchronous manner.
         */
        runBlocking {
            doSomethingSlowly()

            log("The first, parent, coroutine starts")
            launch {
                log("The second coroutine starts and is ready to be suspended")
                delay(100.milliseconds)
                log("The second coroutine is resumed")
            }
            launch {
                log("The third coroutine can run in the meantime")
            }
            log("The first coroutine has launched two more coroutines")
        }
    }
}