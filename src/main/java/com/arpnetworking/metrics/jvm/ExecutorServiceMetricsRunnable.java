/*
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
 * An implementation of {@link Runnable} that collects all metrics for
 * registered {@link ExecutorService} instances each time its run.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
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
     * Generate samples for a {@link ForkJoinPool}.
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
     * @param metrics this unit of work's {@link Metrics} instance
     * @param name the name of the executor service
     * @param executorService the {@link ForkJoinPool} instance to sample
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
     * Generate samples for a {@link ThreadPoolExecutor}.
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
     * @param metrics this unit of work's {@link Metrics} instance
     * @param name the name of the executor service
     * @param executorService the {@link ForkJoinPool} instance to sample
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
     * Builder for {@link ExecutorServiceMetricsRunnable}.
     *
     * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
     */
    // CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
    public static class Builder {
        // CHECKSTYLE.ON: FinalClass

        /**
         * Builds an instance of {@link ExecutorServiceMetricsRunnable}.
         *
         * @return An instance of {@link ExecutorServiceMetricsRunnable}.
         */
        public ExecutorServiceMetricsRunnable build() {
            if (_metricsFactory == null) {
                throw new IllegalArgumentException("MetricsFactory cannot be null.");
            }
            if (_swallowException == null) {
                _swallowException = DEFAULT_SWALLOW_EXCEPTION;
                LOGGER.info(String.format("Defaulted null swallow exception; swallowException=%s", _swallowException));
            }
            if (_executorServices == null) {
                _executorServices = DEFAULT_EXECUTOR_SERVICES;
                LOGGER.info(String.format("Defaulted null executor services; executorServices=%s", _executorServices));
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
         * Set the {@link MetricsFactory} instance. Required. Cannot be
         * null.
         *
         * @param value The value for the {@link MetricsFactory} instance.
         * @return This {@link Builder} instance.
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
         * @param value The value for the {@link Boolean} instance.
         * @return This {@link Builder} instance.
         */
        public Builder setSwallowException(final Boolean value) {
            _swallowException = value;
            return this;
        }

        /**
         * Set the {@link ExecutorService} instances by name. Optional.
         * Defaults to an empty {@link Map}. Cannot be null.
         *
         * @param value The {@link ExecutorService} instances by name.
         * @return This {@link Builder} instance.
         */
        public Builder setExecutorServices(final Map<String, ExecutorService> value) {
            // CHECKSTYLE.OFF: IllegalInstantiation - No Guava here
            _executorServices = value == null ? null : Collections.unmodifiableMap(new HashMap<>(value));
            // CHECKSTYLE.ON: IllegalInstantiation
            return this;
        }

        private MetricsFactory _metricsFactory;
        private Boolean _swallowException = DEFAULT_SWALLOW_EXCEPTION;
        private Map<String, ExecutorService> _executorServices = DEFAULT_EXECUTOR_SERVICES;

        private static final Boolean DEFAULT_SWALLOW_EXCEPTION = true;
        private static final Map<String, ExecutorService> DEFAULT_EXECUTOR_SERVICES = Collections.emptyMap();
    }
}
