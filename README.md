# StackTraceCompactor
World's most advanced stack trace generator! 😃 Make your Java stack traces less verbose without sacrificing information!

![Example](https://github.com/dbeaudoinfortin/StackTraceCompactor/assets/15943629/d3870f4f-b56a-444c-85ac-3d8492e278aa)

## How to use

Add the following to your pom.xml:

```
<dependency>
  <groupId>com.dbf.utils</groupId>
  <artifactId>stack-trace-compactor</artifactId>
  <version>0.0.2</version>
</dependency>
```

And invoke the utility by calling `StackTraceCompactor.getCompactStackTrace(t)` :

```
try {
  //Some fun stuff
} catch (Throwable t) {
  log.error("Unexpected failure. " + StackTraceCompactor.getCompactStackTrace(t));
}
```

## How it works
The stack elements are first generated using the Apache Commons Lang3 library. This produces a reversed order to the exception chain that shows the root cause at the very top. Then the following redundant information is removed:

- As per Java convention, class names and file names should always be the same. When they are, I omit the file name and only retain the line number.
- Portions of the package name that have already been stated in the previous line are abbreviated on subsequent lines.
- Inner classes are treated similarly to the package name and are also condensed.
- Exception messages are often duplicated when an exception is wrapped one or multiple times. When this happens, I omit the redundant information.
- When dealing with wrapped exceptions, common (duplicated) stack frames are omitted. 

## Requirements

- Requires Java 8 or later.
- If you want to build the jar from the source code you will need [Apache Maven](https://maven.apache.org/).
