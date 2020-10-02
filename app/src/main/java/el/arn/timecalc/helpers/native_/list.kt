package el.arn.timecalc.helpers.native_

fun <T>List<T>.prev(item: T): T? {
    if (!contains(item)) { throw NoSuchElementException("item[$item]") }
    val prevItemIndex = indexOf(item)-1
    if (prevItemIndex < 0) {
        return null
    }
    return this[prevItemIndex]
}
fun <T>List<T>.allPrev(item: T): List<T> {
    if (!contains(item)) { throw NoSuchElementException("item[$item]") }
    return slice(0 until indexOf(item))
}

fun <T>List<T>.next(item: T): T? {
    if (!contains(item)) { throw NoSuchElementException("item[$item]") }
    val nextItemIndex = indexOf(item)+1
    if (nextItemIndex > lastIndex) {
        return null
    }
    return this[nextItemIndex]
}
fun <T>List<T>.allNext(item: T): List<T> {
    if (!contains(item)) { throw NoSuchElementException("item[$item]") }
    val nextItemIndex = indexOf(item)+1
    return slice(nextItemIndex..lastIndex)
}

