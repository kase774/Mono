package dev.kason.mono.error

import kotlin.Exception

/** Wraps around an existing [exception] so that it can be handled by the error system.
 * Note that this will only display the stack trace into the output, but will integrate with any other
 * displays that the error system has registered. */
fun CompilerException(exception: Exception): CompilerException {
    // if it already has a handler & context, we shouldn't make the display worse;
    if (exception is CompilerException) return exception
    return CompilerException(SingleItemContext(exception), NonCompilerExceptionHandler)
}

/** Executes the lambda if possible; if not, will use mono error system to display the error */
fun <T> runSafe(block: () -> T): T? = try {

}


private val description = """
    this error is emitted when a non-compiler exception is caught. typically this
    is the result of a bug within the compiler; if you see this error please report it along with
    the stack trace!
""".trimIndent()

// the handler; make sure that the report has the proper cause set
internal object NonCompilerExceptionHandler : Handler<SingleItemContext<Exception>>(Level.Error, 0, description) {
    override fun createReport(context: SingleItemContext<Exception>): Report = Report(
        this,
        "${context.item.javaClass.canonicalName}: ${context.item.message ?: "no message"}",
        context.item,
        listOf()
    )

    fun create
}