# StackTraceCompactor
World's most advanced stack trace generator! 😃 Make your Java stack traces less verbose without sacrificing information!

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

## Requirements

- Requires Java 8 or later.
- If you want to build the jar from the source code you will need [Apache Maven](https://maven.apache.org/).
