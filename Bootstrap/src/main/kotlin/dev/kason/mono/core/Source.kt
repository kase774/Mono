package dev.kason.mono.core

import dev.kason.mono.error.CompilerException
import java.io.File
import java.nio.charset.Charset

/** A [Source] is any object that can produce mono code, such as a file
 * or a string buffer created by a console. Sources create [Doc]s, which are
 * immutable snapshots of the source. When getting the latest doc from a source,
 * the compiler will check to see whether there is a newer version and create a new doc.
 *
 * Sources can belong to a module, which give additional information on how to compile the document.
 * Outside of testing, it is highly recommended to use modules.
 * @see Doc
 * */
abstract class Source(val name: String, val module: Module? = null) {
    abstract fun readSource(): String
    abstract fun shouldUpdate(): Boolean

    var currentDocBuffer: Doc? = null
        private set

    /** Fetches the latest [Doc] from this source. if the current doc buffer isn't the latest,
     * it will call [readSource], which could potentially be blocking. */
    val latestDoc: Doc
        get() {
            if (currentDocBuffer == null || shouldUpdate())
                currentDocBuffer = Doc(readSource(), this)
            return currentDocBuffer!!
        }
}

/** A [Source] representing a file.
 *
 * Notes:
 *  - By default, the name of the file will be the path relative to the os root; this can be changed in the constructor
 *  - The source doc will be updated depending on the file's `lastModified` timestamp, if available.
 *  - The default charset for reading the file is UTF-8; this can be changed in the constructor
 * */
class FileSource(
    val file: File,
    name: String = file.absolutePath,
    val charset: Charset = Charsets.UTF_8,
    module: Module? = null
) : Source(name, module) {

    var lastRead: Long = -1
    override fun readSource(): String = try {
        file.readText(charset)
            .also { lastRead = System.currentTimeMillis() } // set last read only if we actually read it..
    } catch (securityEx: SecurityException) {
        throw CompilerException(securityEx)
    }

    override fun shouldUpdate(): Boolean = try {
        file.lastModified() > lastRead
    } catch (securityEx: SecurityException) {
        throw CompilerException(securityEx)
    }
}

/** Creates a source from a string. Since the value is always the same, it won't ever update */
class StringSource(val sourceCode: String, name: String = "<string source>", module: Module? = null) :
    Source(name, module) {
    override fun readSource(): String = sourceCode
    override fun shouldUpdate(): Boolean = false
}

// For this project, \n will be used as the newline (Mac & UNIX style)
// therefore, we'll try to convert Windows style (\r\n) or very old Mac style (\r)
// text into something the compiler can parse
private fun convertNewlineStyles(rawSourceCode: String): String =
    rawSourceCode.replace("\r\n", "\n") // windows
        .replace("\r", "\n") // pre mac os x 10

class Doc(rawSourceCode: String, val source: Source) : CharSequence {
    val sourceCode = convertNewlineStyles(rawSourceCode)
    val timestamp: Long = System.currentTimeMillis()

    // implement the functions required by char seq
    override val length: Int = sourceCode.length
    override operator fun get(index: Int): Char = sourceCode[index]
    override fun subSequence(startIndex: Int, endIndex: Int): String = sourceCode.substring(startIndex, endIndex)

    /** Load the indices of the newlines in [sourceCode] here. We can use this to quickly access
     * the indices of characters based on the line and column.
     *
     * newLineIndices is padded with -1 & length such that `newLineIndices[n]` will be the beginning
     * newline for a given line *n*, and `newLineIndices[n+1]` will be the end. Note that lines are 1-based.
     * */
    private val newLineIndices = listOf(-1) + sourceCode.indices.filter { sourceCode[it] == '\n' } + length

    inner class Line(val lineNumber: Int): CharSequence {
        val lineStart = Index(this@Doc, )
    }

}