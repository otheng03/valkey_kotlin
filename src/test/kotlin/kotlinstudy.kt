package study

import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.Test

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
}