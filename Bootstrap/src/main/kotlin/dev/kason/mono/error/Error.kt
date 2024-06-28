package dev.kason.mono.error

// For information about the error system, check the docs!!

/** A parent class representing an exception that was created by the compiler.
 * When thrown, the compiler exception will generate a [Report], informing the compiler
 * error system that something is wrong. This will allow the compiler error system to parse the
 * report and give it to the user. Meanwhile, the exception will propagate until it is caught, returning
 * the compilation to a safe location where it can be resumed.
 *
 * At this `resume location`, there is no need to broadcast the error, since the compiler error system
 * has already handled all of that. */
class CompilerException internal constructor(val report: Report) : Exception(report.message, report.causedBy) {
    /** an object that contains the information that needs to be passed to the compiler. */
    interface Context
}

// Helper function that acts as a constructor since subclasses of Throwable can't have generics.
/** @see CompilerException */
fun <T : CompilerException.Context> CompilerException(context: T, handler: Handler<T>): CompilerException =
    CompilerException(handler.createReport(context))

/** A handler is a class that converts a [CompilerException.Context] into a [Report]. Since
 * each context is different depending on the error type, there should be a handler for each type of
 * error. Because of this, `mono` will also often use Handlers as a representation of the error type, which
 * is why they include information such as the level of the error, or the code. */
abstract class Handler<T : CompilerException.Context>(
    val level: Level,
    // the number representing this handler: for example, E0999 would have 999 as the numeric code
    val numericCode: Int,
    // what shows up when you ask for an explanation
    val description: String,
) {
    enum class Level {
        Error, // fatal, cannot continue
        Warning, // technically allowed, but still problematic
        Lint // simple problems, such as formatting; stuff that doesn't really matter
    }

    /** Function that creates a report based on the context. Try to avoid any possible sources of exception here,
     * since it will prevent this error from being reported :anxious: */
    abstract fun createReport(context: T): Report
}

/**
 *
 *
 *
 * */
class Report(
    val handler: Handler<*>,
    val message: String,
    val causedBy: Throwable? = null,
    val components: List<Component>
) {
    interface Component
}