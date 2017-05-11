/**
 * Copyright 2017 Inscope Metrics Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.metrics.jvm;

import com.arpnetworking.metrics.Metrics;
import com.arpnetworking.metrics.MetricsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * An implementation of <code>Runnable</code> that collects all metrics for
 * registered <code>ExecutorService</code> instances each time its run.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
// CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
public class ExecutorServiceMetricsRunnable extends AbstractMetricsRunnable {
// CHECKSTYLE.ON: FinalClass

    @Override
    protected void collectMetrics(final Metrics metrics) {
        for (final Map.Entry<String, ExecutorService> entry : _executorServices.entrySet()) {
            final String name = entry.getKey();
            final ExecutorService executorService = entry.getValue();
            if (executorService instanceof ForkJoinPool) {
                processForkJoinPool(metrics, name, (ForkJoinPool) executorService);
            }
            if (executorService instanceof ThreadPoolExecutor) {
                // NOTE: That a ScheduledThreadPoolExecutor is a ThreadPoolExecutor
                processThreadPoolExecutor(metrics, name, (ThreadPoolExecutor) executorService);
            }
        }
    }

    /**
     * Generate samples for a <code>ForkJoinPool</code>.
     *
     * Includes metrics for:
     * <ul>
     *     <li>active_threads</li>
     *     <li>queued_submissions</li>
     *     <li>queued_tasks</li>
     *     <li>parallelism</li>
     *     <li>thread_pool_size</li>
     * </ul>
     *
     * @param metrics this unit of work's <code>Metrics</code> instance
     * @param name the name of the executor service
     * @param executorService the <code>ForkJoinPool</code> instance to sample
     */
    protected void processForkJoinPool(
            final Metrics metrics,
            final String name,
            final ForkJoinPool executorService) {

        final String prefix = String.join(
                "/",
                ROOT_NAMESPACE,
                name);
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "active_threads"),
                executorService.getActiveThreadCount());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "queued_submissions"),
                executorService.getQueuedSubmissionCount());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "queued_tasks"),
                executorService.getQueuedTaskCount());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "parallelism"),
                executorService.getParallelism());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "thread_pool_size"),
                executorService.getPoolSize());
    }

    /**
     * Generate samples for a <code>ThreadPoolExecutor</code>.
     *
     * Includes metrics for:
     * <ul>
     *     <li>active_threads</li>
     *     <li>queued_tasks</li>
     *     <li>completed_tasks <i>(for all time)</i></li>
     *     <li>thread_pool_maximum_size</li>
     *     <li>thread_pool_size</li>
     * </ul>
     *
     * @param metrics this unit of work's <code>Metrics</code> instance
     * @param name the name of the executor service
     * @param executorService the <code>ForkJoinPool</code> instance to sample
     */
    protected void processThreadPoolExecutor(
            final Metrics metrics,
            final String name,
            final ThreadPoolExecutor executorService) {

        final String prefix = String.join(
                "/",
                ROOT_NAMESPACE,
                name);
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "active_threads"),
                executorService.getActiveCount());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "queued_tasks"),
                executorService.getQueue().size());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "completed_tasks"),
                executorService.getCompletedTaskCount());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "thread_pool_maximum_size"),
                executorService.getMaximumPoolSize());
        metrics.setGauge(
                String.join(
                        "/",
                        prefix,
                        "thread_pool_size"),
                executorService.getPoolSize());
    }

    /**
     * Protected constructor.
     *
     * @param builder instance of {@link Builder}
     */
    protected ExecutorServiceMetricsRunnable(final Builder builder) {
        super(builder._metricsFactory, builder._swallowException, LOGGER);
        _executorServices = builder._executorServices;
    }

    private final Map<String, ExecutorService> _executorServices;

    private static final String ROOT_NAMESPACE = "executor_services";
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmMetricsRunnable.class);

    /**
     * Builder for <code>ExecutorServiceMetricsRunnable</code>.
     *
     * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
     */
    // CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
    public static class Builder {
        // CHECKSTYLE.ON: FinalClass

        /**
         * Builds an instance of <code>ExecutorServiceMetricsRunnable</code>.
         *
         * @return An instance of <code>ExecutorServiceMetricsRunnable</code>.
         */
        public ExecutorServiceMetricsRunnable build() {
            if (_metricsFactory == null) {
                throw new IllegalArgumentException("MetricsFactory cannot be null.");
            }
            if (_swallowException == null) {
                throw new IllegalArgumentException("SwallowException cannot be null.");
            }
            if (_executorServices == null) {
                throw new IllegalArgumentException("ExecutorServices cannot be null.");
            }
            for (final ExecutorService executorService : _executorServices.values()) {
                // NOTE: That a ScheduledThreadPoolExecutor is a ThreadPoolExecutor
                if (!(executorService instanceof ForkJoinPool)
                        && !(executorService instanceof ThreadPoolExecutor)) {
                    throw new IllegalArgumentException(
                            "Unsupported ExecutorService type: " + executorService.getClass().getName());
                }
            }
            return new ExecutorServiceMetricsRunnable(this);
        }

        /**
         * Set the <code>MetricsFactory</code> instance. Required. Cannot be
         * null.
         *
         * @param value The value for the <code>MetricsFactory</code> instance.
         * @return This <code>Builder</code> instance.
         */
        public Builder setMetricsFactory(final MetricsFactory value) {
            _metricsFactory = value;
            return this;
        }

        /**
         * Set the flag indicating if any exception caught during the process
         * of metrics collection should be logged and swallowed. Optional.
         * Defaults to true. Cannot be null. True indicates that the exception
         * will be logged and swallowed false indicates it will be rethrown as
         * a {@code RuntimeException}.
         *
         * @param value The value for the <code>Boolean</code> instance.
         * @return This <code>Builder</code> instance.
         */
        public Builder setSwallowException(final Boolean value) {
            _swallowException = value;
            return this;
        }

        /**
         * Set the <code>ExecutorService</code> instances by name. Optional.
         * Defaults to an empty <code>Map</code>. Cannot be null.
         *
         * @param value The <code>ExecutorService</code> instances by name.
         * @return This <code>Builder</code> instance.
         */
        public Builder setExecutorServices(final Map<String, ExecutorService> value) {
            // CHECKSTYLE.OFF: IllegalInstantiation - No Guava here
            _executorServices = Collections.unmodifiableMap(new HashMap<>(value));
            // CHECKSTYLE.ON: IllegalInstantiation
            return this;
        }

        private MetricsFactory _metricsFactory;
        private Boolean _swallowException = true;
        private Map<String, ExecutorService> _executorServices;
    }
}
