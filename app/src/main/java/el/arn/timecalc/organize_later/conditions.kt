package el.arn.timecalc.organize_later

import java.util.*

open class ValueSet<T> : TreeSet<T>() {

    fun add(v1: T, v2: T) {
        add(v1)
        add(v2)
    }

    fun add(v1: ValueSet<T>, v2: T) {
        addAll(v1)
        add(v2)
    }

}

class AndSet<T> : ValueSet<T>()
class OrSet<T> : ValueSet<T>()

infix fun <T> T.equals(list: ValueSet<T>): Boolean {
    if (list is OrSet<*>) return this in list
    if (list is AndSet<*>) return list.find { it != this } == null
    error("Unsupported set type: ${list::class.simpleName}")
}

infix fun <T> T.or(second: T) = OrSet<T>().also { it.add(this, second) }
infix fun <T> OrSet<T>.or(second: T) = OrSet<T>().also { it.add(this, second) }

infix fun <T> T.and(second: T) = AndSet<T>().also { it.add(this, second) }
infix fun <T> AndSet<T>.and(second: T) = AndSet<T>().also { it.add(this, second) }

fun main() {

    val test1 = "a"
    val test2 = "b"
    val test3 = "c"
    val test4 = "d"

    println(test1 equals ("b" or "c" or "d")) // Prints: false
    println(test2 equals ("b" or "c" or "d")) // Prints: true
    println(test3 equals ("b" or "c" or "d")) // Prints: true
    println(test4 equals ("b" or "c" or "d")) // Prints: true

    val test5 = "a"
    val test6 = "a"
    val test7 = "a"
    val test8 = "b"

    println("a" equals (test5 and test6 and test7)) // Prints: true
    println("a" equals (test5 and test6 and test8)) // Prints: false

}