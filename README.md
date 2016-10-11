Jvm Extra
=========

<a href="https://raw.githubusercontent.com/ArpNetworking/metrics-jvm-extra/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg"
         alt="License: Apache 2">
</a>
<a href="https://travis-ci.org/ArpNetworking/metrics-jvm-extra/">
    <img src="https://travis-ci.org/ArpNetworking/metrics-jvm-extra.png?branch=master"
         alt="Travis Build">
</a>
<a href="http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.metrics.extras%22%20a%3A%22jvm-extra%22">
    <img src="https://img.shields.io/maven-central/v/com.arpnetworking.metrics.extras/jvm-extra.svg"
         alt="Maven Artifact">
</a>

A runnable to collect the various JVM metrics.


Instrumenting Your JVM Application
----------------------------------

### Add Dependency

Determine the latest version of the Java client in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.arpnetworking.metrics%22%20a%3A%22jvm-extra%22).

#### Maven

Add a dependency to your pom:

```xml
<dependency>
    <groupId>com.arpnetworking.metrics.extras</groupId>
    <artifactId>jvm-extra</artifactId>
    <version>VERSION</version>
</dependency>
```

The Maven Central repository is included by default.

#### Gradle

Add a dependency to your build.gradle:

    compile group: 'com.arpnetworking.metrics.extras', name: 'jvm-extra', version: 'VERSION'

Add the Maven Central Repository into your *build.gradle*:

```groovy
repositories {
    mavenCentral()
}
```

#### SBT

Add a dependency to your project/Build.scala:

```scala
val appDependencies = Seq(
    "com.arpnetworking.metrics" % "jvm-extra" % "VERSION"
)
```

The Maven Central repository is included by default.

#### Vertx

Users of Vertx need to also depend on the vertx-extra package.  The vertx-extra provides the necessary wrappers around the standard Java metrics client to work with the shared data model in Vertx.  Special thanks to Gil Markham for contributing this work.  For more information please see [metrics-vertx-extra/README.md](https://github.com/ArpNetworking/metrics-client-java).

### JvmMetricsRunnable

The only dependency of the JvmMetricsRunnable is an instance of MetricsFactory from the Java metrics client. By default all defined JVM metrics are collected. Further, any exceptions encountered during collection are logged and swallowed. To instantiate a default JvmMetricsRunnable do the following:

```java
JvmMetricsRunnable.Builder
    .newInstance()
    .setMetricsFactory(_metricsFactory)
    .build();
```

If you prefer to handle exceptions then you will need to explicitly set the swallowException attribute to false. For example:

```java
JvmMetricsRunnable.Builder
    .newInstance()
    .setMetricsFactory(metricsFactory)
    .setSwallowException(false)
    .build();
```

If you prefer to not collect some metrics then disable those collectors. For example:

```java
JvmMetricsRunnable.Builder()
    .newInstance()
    .setMetricsFactory(metricsFactory)
    .setCollectGarbageCollectionMetrics(false)
    .setCollectThreadMetrics(false)
    .setCollectNonHeapMemoryMetrics(false)
    .setCollectHeapMemoryMetrics(false)
    .build();
```

#### Executing with ScheduledExecutorService

Using [ScheduledExecutorService](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ScheduledExecutorService.html), you will only need to schedule the JvmMetricsRunnable with an initial delay and a collection interval in the specified time unit.

```java
import com.arpnetworking.metrics.MetricsFactory;
import com.arpnetworking.metrics.Sink;
import com.arpnetworking.metrics.impl.TsdMetricsFactory;
import com.arpnetworking.metrics.impl.TsdQueryLogSink;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JvmMetricsCollector {

    public static final void main(String[] args) {
        // Instantiate a MetricsFactory
        // NOTE: The first argument is the path to write metrics to
        final Sink sink = new TsdQueryLogSink.Builder()
                .setPath(args[0])
                .build();
        final MetricsFactory metricsFactory = new TsdMetricsFactory.Builder()
                .setSinks(Arrays.asList(sink))
                .build();

        // Create a default JvmMetricsRunnable
        final Runnable runnable = new JvmMetricsRunnable.Builder()
                .setMetricsFactory(metricsFactory)
                .build();

        // Schedule JVM metrics collection
        final ScheduledExecutorService jvmMetricsCollector = Executors.newSingleThreadScheduledExecutor();
        jvmMetricsCollector.scheduleAtFixedRate(
                runnable,
                0, // Initial delay
                500, // Collection interval
                TimeUnit.MILLISECONDS);

        // Let the collector run for a while before shutting down
        Thread.sleep(60000);
        jvmMetricsCollector.shutdown();
    }
}
```

### Executing with Akka Actor

Create a new actor that inherits from the UntypedActor class. Next, schedule a message on the actor with an initial delay and a collection interval to trigger the metrics collection. One way to do this is to override the preStart hook. Finally, cancel the scheduling when the actor stops. In the example below, we use a String message "COLLECT" to trigger the JVM metrics collection by the actor.

```java
import akka.actor.Cancellable;
import akka.actor.UntypedActor;
import com.arpnetworking.metrics.MetricsFactory;
import com.arpnetworking.metrics.jvm.JvmMetricsRunnable;
import com.arpnetworking.metrics.org.joda.time.Period;
import scala.concurrent.duration.FiniteDuration;
import scala.concurrent.duration.TimeUnit;

public class JvmMetricsCollector extends UntypedActor {

    public JvmMetricsCollector(
            final Period interval,
            final MetricsFactory metricsFactory) {
        interval = FiniteDuration.create(
                interval.getMillis(),
                TimeUnit.MILLISECONDS);
        jvmMetricsRunnable = new JvmMetricsRunnable.Builder()
                .setMetricsFactory(metricsFactory)
                .setSwallowException(false) // Relying on the default akka supervisor strategy here.
                .build();
    }

    @Override
    public void preStart() {
        cancellable = getContext().system().scheduler().schedule(
                FiniteDuration.Zero(), // Initial delay
                interval, // Collection interval
                self(),
                COLLECT_MESSAGE,
                getContext().system().dispatcher(),
                self());
    }

    @Override
    public void postStop() {
        cancellable.cancel();
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        if (COLLECT_MESSAGE.equals(message)) {
            jvmMetricsRunnable.run();
        } else {
            unhandled(message);
        }
    }

    private Cancellable cancellable;
    private final FiniteDuration interval;
    private final Runnable jvmMetricsRunnable;

    private static final String COLLECT_MESSAGE = "COLLECT";
}
```

Building
--------

Prerequisites:
* [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Building:

    metrics-jvm-extra> ./mvnw verify

To use the local version you must first install it locally:

    metrics-jvm-extra> ./mvnw install

You can determine the version of the local build from the pom file.  Using the local version is intended only for testing or development.

You may also need to add the local repository to your build in order to pick-up the local version:

* Maven - Included by default.
* Gradle - Add *mavenLocal()* to *build.gradle* in the *repositories* block.
* SBT - Add *resolvers += Resolver.mavenLocal* into *project/plugins.sbt*.

License
-------

Published under Apache Software License 2.0, see LICENSE

&copy; Groupon Inc., 2015
