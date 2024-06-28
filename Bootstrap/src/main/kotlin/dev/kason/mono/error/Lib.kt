package dev.kason.mono.error

/** A convenient [CompilerException.Context] for when there's only 1 item; it's easier than creating
 * a whole new class each time.
 *
 * @see CompilerException.Context */
class SingleItemContext<T>(val item: T): CompilerException.Context