flume-delay-interceptor
=================

A simple Flume interceptor that introduces a configurable delay after it reads a file.

Useful if you have many large files to ingest or a slow sink and want to delay the source before running out of Java heap.


# Usage
Copy it to Flume lib dir (`/usr/hdp/current/flume-server/lib/` in HDP).

Activate it on the source that needs to be delayed:

```
a1.sources.s1.interceptors = delay1
a1.sources.s1.interceptors.delay1.type=org.apache.flume.interceptor.FlumeDelayInterceptor$Builder
a1.sources.s1.interceptors.delay1.interceptorDelay=5000
```


# How to compile
To compile it, use Maven:

```
mvn clean package
```
