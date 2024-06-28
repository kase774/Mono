package dev.kason.mono.core

/** represents a single location within a document. has many utilities to make working with
 * these easier. */
class Index(
    val doc: Doc,
    /** The index relative to the start of the doc (0 is first char) */
    val index: Int,
    // line & column are 1-based
    val line: Int,
    val column: Int
) : Comparable<Index> {
    override fun compareTo(other: Index): Int {
        TODO("Not yet implemented")
    }

}