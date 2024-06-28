## An overview of the `mono` error system.

Errors are bound to happen in any complex project, but especially one that deals with as much user input as a compiler. 
Luckily, the Mono error system is there to make the process of reporting errors as smooth as possible. This file describes how
that system works, and how to use it in the mono compiler development process.

### Tl;dr
At the site of the problem, a compiler exception is created with a handler and context. The details of the error, in the form 
of a context (which is unique to the error type), are passed to the relevant context handler, creating a report, which is a standardized object. 
This report is given to the error reporting system which parses it and outputs an error.
Meanwhile, the exception travels through the stack until it reaches a catch site, where compilation can be resumed.

Here's a diagram that shows the overall process:
```
 
[error ] --- creates --> { exception } 
                               |
                  [handler] ---+--- { context }      (error sys)
                      |                                  |
                      +---- generates --> [report] --- routed ----> [user]
                  
```


### Description

The process of handling an error begins when an `dev.kason.mono.error.CompilerException` is thrown.
Each compiler exception must have 2 parameters 
 - context on the error for the handler
 - a handler that can receive the context and create a report for it

The context simply gives the handler details on what specific error. For example, if we want to implement a system
for the case in which a string token is unterminated, the context simply contain the starting index of the token or the token
itself. Any context works, as long as its handler understands what to do with the information.

> The process of adding a handler can be simplified by making a function that passes a specific handler to its parent.
> 
> For example:
> ```kt
> throw CompilerException(context, handler)
> ```
> 
> If we're making a lot of exceptions with the same handler, we can create a function that passes that handler upstream for us
> and save us a bit of trouble.
> ```kt
> fun ACompilerException(context: AContext): CompilerException = CompilerException(context, aHandler)
> 
> throw ACompilerException(context)
> ```

After a compiler exception is thrown, the handler will be used to process the context into a report.
Each error type should have its own handler, and all exceptions of that error type should use that handler.

Back to the example of the string token being unterminated. A handler would take that context, in the form of 
`(starting_index, token)` and convert it to a report object, which may look like

```json
{
    "code": 932
    "message": "string token unterminated at src/sample.mono 5:12:123", 
    "level": "error", 
    /* more report properties */
}
```

Essentially, a report is a standardized object that the compiler can recognize and use. Unlike context, which can have
varying properties for different error types, all reports have the same structure.

Next, the mono error system converts this report and processes it to display it to the user. For example, it could format the
report to a human-readable text block to output to the terminal, or it could format it as json and send it to the IDE. 
Either way, at this point, the compiler developer doesn't need to do anything anymore.

However, note that the `CompilerException` class is a `Throwable`; this means that it will pass through the stack
like any other throwable until it reaches a safe place to be caught. This place is where the compiler attempts to resume
with the compilation process, if possible.
