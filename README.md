# StackTraceCompactor
World's most advanced stack trace generator! 😃 Make your Java stack traces less verbose without sacrificing information!

![Example](https://github.com/dbeaudoinfortin/StackTraceCompactor/assets/15943629/d3870f4f-b56a-444c-85ac-3d8492e278aa)

A 50% reduction.

## How to use

Add the following to your pom.xml:

```
<dependency>
  <groupId>io.github.dbeaudoinfortin</groupId>
  <artifactId>stack-trace-compactor</artifactId>
  <version>0.0.3</version>
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

## Legal Stuff

Copyright (c) 2024 David Fortin

This software is provided by David Fortin under the MIT License, meaning you are free to use it however you want, as long as you include the original copyright notice (above) and license notice in any copy you make. You just can't hold me liable in case something goes wrong. License details can be read [here](https://github.com/dbeaudoinfortin/StackTraceCompactor?tab=MIT-1-ov-file)
