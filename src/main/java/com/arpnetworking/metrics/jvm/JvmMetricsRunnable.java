/*
 * Copyright 2015 Groupon.com
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
import com.arpnetworking.metrics.jvm.collectors.BufferPoolMetricsCollector;
import com.arpnetworking.metrics.jvm.collectors.FileDescriptorMetricsCollector;
import com.arpnetworking.metrics.jvm.collectors.GarbageCollectionMetricsCollector;
import com.arpnetworking.metrics.jvm.collectors.HeapMemoryMetricsCollector;
import com.arpnetworking.metrics.jvm.collectors.JvmMetricsCollector;
import com.arpnetworking.metrics.jvm.collectors.PoolMemoryMetricsCollector;
import com.arpnetworking.metrics.jvm.collectors.ThreadMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of <code>Runnable</code> that collects all JVM metrics
 * each time its run.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
// CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
public class JvmMetricsRunnable extends AbstractMetricsRunnable {
// CHECKSTYLE.ON: FinalClass

    @Override
    protected void collectMetrics(final Metrics metrics) {
        for (final JvmMetricsCollector collector : _collectorsEnabled) {
            collector.collect(metrics, _managementFactory);
        }
    }

    private JvmMetricsRunnable(final Builder builder) {
        super(builder._metricsFactory, builder._swallowException, LOGGER);
        _managementFactory = builder._managementFactory;
        if (builder._collectGarbageCollectionMetrics) {
            _collectorsEnabled.add(builder._garbageCollectionMetricsCollector);
        }
        if (builder._collectHeapMemoryMetrics) {
            _collectorsEnabled.add(builder._heapMemoryMetricsCollector);
        }
        if (builder._collectNonHeapMemoryMetrics) {
            _collectorsEnabled.add(builder._nonHeapMemoryMetricsCollector);
        }
        if (builder._collectPoolMemoryMetrics) {
            _collectorsEnabled.add(builder._poolMemoryMetricsCollector);
        }
        if (builder._collectThreadMetrics) {
            _collectorsEnabled.add(builder._threadMetricsCollector);
        }
        if (builder._collectBufferPoolMetrics) {
            _collectorsEnabled.add(builder._bufferPoolMetricsCollector);
        }
        if (builder._collectFileDescriptorMetrics) {
            _collectorsEnabled.add(builder._fileDescriptorMetricsCollector);
        }
    }

    private final ManagementFactory _managementFactory;
    private final List<JvmMetricsCollector> _collectorsEnabled = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(JvmMetricsRunnable.class);

    /**
     * Builder for <code>JvmMetricsRunnable</code>.
     *
     * @author Deepika Misra (deepika at groupon dot com)
     */
    // CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
    public static class Builder {
        // CHECKSTYLE.ON: FinalClass

        /**
         * Builds an instance of <code>JvmMetricsRunnable</code>.
         *
         * @return An instance of <code>JvmMetricsRunnable</code>.
         */
        public JvmMetricsRunnable build() {
            if (_metricsFactory == null) {
                throw new IllegalArgumentException("MetricsFactory cannot be null.");
            }
            if (_managementFactory == null) {
                _managementFactory = DEFAULT_MANAGEMENT_FACTORY;
                LOGGER.info(String.format(
                        "Defaulted null management factory; managementFactory=%s",
                        _managementFactory));
            }
            if (_swallowException == null) {
                _swallowException = DEFAULT_SWALLOW_EXCEPTION;
                LOGGER.info(String.format(
                        "Defaulted null swallow exception; swallowException=%s",
                        _swallowException));
            }
            // TODO(ville): The collection/collector pattern should be formalized.
            defaultCollection();
            defaultCollectors();
            return new JvmMetricsRunnable(this);
        }

        private void defaultCollection() {
            if (_collectNonHeapMemoryMetrics == null) {
                _collectNonHeapMemoryMetrics = DEFAULT_COLLECT_NON_HEAP_MEMORY_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect non heap memory metrics; collectNonHeapMemoryMetrics=%s",
                        _collectNonHeapMemoryMetrics));
            }
            if (_collectPoolMemoryMetrics == null) {
                _collectPoolMemoryMetrics = DEFAULT_COLLECT_POOL_MEMORY_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect pool memory metrics; collectPoolMemoryMetrics=%s",
                        _collectPoolMemoryMetrics));
            }
            if (_collectHeapMemoryMetrics == null) {
                _collectHeapMemoryMetrics = DEFAULT_COLLECT_HEAP_MEMORY_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect heap memory metrics; collectHeapMemoryMetrics=%s",
                        _collectHeapMemoryMetrics));
            }
            if (_collectThreadMetrics == null) {
                _collectThreadMetrics = DEFAULT_COLLECT_THREAD_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect thread metrics; collectThreadMetrics=%s",
                        _collectThreadMetrics));
            }
            if (_collectGarbageCollectionMetrics == null) {
                _collectGarbageCollectionMetrics = DEFAULT_COLLECT_GC_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect garbage collection metrics; collectGarbageCollectionMetrics=%s",
                        _collectGarbageCollectionMetrics));
            }
            if (_collectBufferPoolMetrics == null) {
                _collectBufferPoolMetrics = DEFAULT_COLLECT_BUFFER_POOL_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect buffer pool metrics; collectBufferPoolMetrics=%s",
                        _collectBufferPoolMetrics));
            }
            if (_collectFileDescriptorMetrics == null) {
                _collectFileDescriptorMetrics = DEFAULT_COLLECT_FILE_DESCRIPTOR_METRICS;
                LOGGER.info(String.format(
                        "Defaulted null collect file descriptor metrics; collectFileDescriptorMetrics=%s",
                        _collectFileDescriptorMetrics));
            }
        }

        private void defaultCollectors() {
            if (_nonHeapMemoryMetricsCollector == null) {
                _nonHeapMemoryMetricsCollector = DEFAULT_HEAP_MEMORY_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null heap memory metrics collector; nonHeapMemoryMetricsCollector=%s",
                        _nonHeapMemoryMetricsCollector));
            }
            if (_poolMemoryMetricsCollector == null) {
                _poolMemoryMetricsCollector = DEFAULT_POOL_MEMORY_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null pool memory metrics collector; poolMemoryMetricsCollector=%s",
                        _poolMemoryMetricsCollector));
            }
            if (_heapMemoryMetricsCollector == null) {
                _heapMemoryMetricsCollector = DEFAULT_HEAP_MEMORY_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null heap memory metrics collector; heapMemoryMetricsCollector=%s",
                        _heapMemoryMetricsCollector));
            }
            if (_threadMetricsCollector == null) {
                _threadMetricsCollector = DEFAULT_THREAD_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null thread metrics collector; threadMetricsCollector=%s",
                        _threadMetricsCollector));
            }
            if (_garbageCollectionMetricsCollector == null) {
                _garbageCollectionMetricsCollector = DEFAULT_GC_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null garbage collector metrics collector; garbageCollectionMetricsCollector=%s",
                        _garbageCollectionMetricsCollector));
            }
            if (_bufferPoolMetricsCollector == null) {
                _bufferPoolMetricsCollector = DEFAULT_BUFFER_POOL_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null buffer pool metrics collector; bufferPoolMetricsCollector=%s",
                        _bufferPoolMetricsCollector));
            }
            if (_fileDescriptorMetricsCollector == null) {
                _fileDescriptorMetricsCollector = DEFAULT_FILE_DESCRIPTOR_METRICS_COLLECTOR;
                LOGGER.info(String.format(
                        "Defaulted null file descriptor metrics collector; fileDescriptorMetricsCollector=%s",
                        _fileDescriptorMetricsCollector));
            }
        }

        /**
         * Set the <code>MetricsFactory</code> instance. Required. Cannot be null.
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
         * Set the flag indicating if Heap Memory metrics should be collected.
         * A true value indicates that these metrics need to be collected.
         * Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         */
        public Builder setCollectHeapMemoryMetrics(final Boolean value) {
            _collectHeapMemoryMetrics = value;
            return this;
        }

        /**
         * Set the flag indicating if Non-Heap Memory metrics should be
         * collected. A true value indicates that these metrics need to
         * be collected. Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         * @deprecated Use the PoolMemoryMetricsCollector
         */
        @Deprecated
        public Builder setCollectNonHeapMemoryMetrics(final Boolean value) {
            _collectNonHeapMemoryMetrics = value;
            return this;
        }

        /**
         * Set the flag indicating if Non-Heap Memory metrics should be
         * collected. A true value indicates that these metrics need to
         * be collected. Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         */
        public Builder setCollectPoolMemoryMetrics(final Boolean value) {
            _collectPoolMemoryMetrics = value;
            return this;
        }

        /**
         * Set the flag indicating if Thread metrics should be collected. A
         * true value indicates that these metrics need to be collected.
         * Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         */
        public Builder setCollectThreadMetrics(final Boolean value) {
            _collectThreadMetrics = value;
            return this;
        }

        /**
         * Set the flag indicating if Garbage Collection metrics should be
         * collected. A true value indicates that these metrics need to be
         * collected. Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         */
        public Builder setCollectGarbageCollectionMetrics(final Boolean value) {
            _collectGarbageCollectionMetrics = value;
            return this;
        }

        /**
         * Set the flag indicating if Buffer Pool metrics should be collected.
         * A true value indicates that these metrics need to be collected.
         * Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         */
        public Builder setCollectBufferPoolMetrics(final Boolean value) {
            _collectBufferPoolMetrics = value;
            return this;
        }

        /**
         * Set the flag indicating if File Descriptor metrics should be
         * collected. A true value indicates that these metrics need to be
         * collected. Optional. Defaults to true. Cannot be null.
         *
         * @param value A <code>Boolean</code> value.
         * @return This <code>Builder</code> instance.
         */
        public Builder setCollectFileDescriptorMetrics(final Boolean value) {
            _collectFileDescriptorMetrics = value;
            return this;
        }

        /**
         * Set the <code>ManagementFactory</code> instance. Optional. Defaults
         * to an instance of <code>ManagementFactoryDefault</code>. Cannot be
         * null. This is for testing purposes only and should never be used by
         * clients.
         *
         * @param value The value for the <code>ManagementFactory</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setManagementFactory(final ManagementFactory value) {
            _managementFactory = value;
            return this;
        }

        /**
         * Set the <code>HeapMemoryMetricsCollector</code>. Defaults to an
         * instance of <code>HeapMemoryMetricsCollector</code>. Cannot be null.
         * This is for testing purposes only and should never be used by
         * clients.
         *
         * @param value A <code>HeapMemoryMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setHeapMemoryMetricsCollector(final JvmMetricsCollector value) {
            _heapMemoryMetricsCollector = value;
            return this;
        }

        /**
         * Set the <code>NonHeapMemoryMetricsCollector</code>. Defaults to an
         * instance of <code>NonHeapMemoryMetricsCollector</code>.  Cannot be
         * null. This is for testing purposes only and should never be used by
         * clients.
         *
         * @param value A <code>NonHeapMemoryMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         * @deprecated Set the PoolMemoryMetricsCollector instead
         */
        @Deprecated
        /* package private */ Builder setNonHeapMemoryMetricsCollector(final JvmMetricsCollector value) {
            _nonHeapMemoryMetricsCollector = value;
            return this;
        }

        /**
         * Set the <code>PoolMemoryMetricsCollector</code>. Defaults to an
         * instance of <code>PoolMemoryMetricsCollector</code>.  Cannot be
         * null. This is for testing purposes only and should never be used by
         * clients.
         *
         * @param value A <code>PoolMemoryMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setPoolMemoryMetricsCollector(final JvmMetricsCollector value) {
            _poolMemoryMetricsCollector = value;
            return this;
        }

        /**
         * Set the <code>ThreadMetricsCollector</code>. Defaults to an
         * instance of <code>ThreadMetricsCollector</code>.  Cannot be null.
         * This is for testing purposes only and should never be used by
         * clients.
         *
         * @param value A <code>ThreadMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setThreadMetricsCollector(final JvmMetricsCollector value) {
            _threadMetricsCollector = value;
            return this;
        }

        /**
         * Set the <code>GarbageCollectionMetricsCollector</code>. Defaults to
         * an instance of <code>GarbageCollectionMetricsCollector</code>.
         * Cannot be null. This is for testing purposes only and should never
         * be used by clients.
         *
         * @param value A <code>GarbageCollectionMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setGarbageCollectionMetricsCollector(final JvmMetricsCollector value) {
            _garbageCollectionMetricsCollector = value;
            return this;
        }

        /**
         * Set the <code>BufferPoolMetricsCollector</code>. Defaults to an
         * instance of <code>BufferPoolMetricsCollector</code>.  Cannot be
         * null. This is for testing purposes only and should never be used by
         * clients.
         *
         * @param value A <code>BufferPoolMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setBufferPoolMetricsCollector(final JvmMetricsCollector value) {
            _bufferPoolMetricsCollector = value;
            return this;
        }

        /**
         * Set the <code>FileDescriptorMetricsCollector</code>. Defaults to
         * an instance of <code>FileDescriptorMetricsCollector</code>. Cannot
         * be null. This is for testing purposes only and should never be used
         * by clients.
         *
         * @param value A <code>FileDescriptorMetricsCollector</code> instance.
         * @return This <code>Builder</code> instance.
         */
        /* package private */ Builder setFileDescriptorMetricsCollector(final JvmMetricsCollector value) {
            _fileDescriptorMetricsCollector = value;
            return this;
        }

        private MetricsFactory _metricsFactory;
        private ManagementFactory _managementFactory = DEFAULT_MANAGEMENT_FACTORY;
        private Boolean _swallowException = DEFAULT_SWALLOW_EXCEPTION;
        private Boolean _collectNonHeapMemoryMetrics = DEFAULT_COLLECT_NON_HEAP_MEMORY_METRICS;
        private Boolean _collectPoolMemoryMetrics = DEFAULT_COLLECT_POOL_MEMORY_METRICS;
        private Boolean _collectHeapMemoryMetrics = DEFAULT_COLLECT_HEAP_MEMORY_METRICS;
        private Boolean _collectThreadMetrics = DEFAULT_COLLECT_THREAD_METRICS;
        private Boolean _collectGarbageCollectionMetrics = DEFAULT_COLLECT_GC_METRICS;
        private Boolean _collectBufferPoolMetrics = DEFAULT_COLLECT_BUFFER_POOL_METRICS;
        private Boolean _collectFileDescriptorMetrics = DEFAULT_COLLECT_FILE_DESCRIPTOR_METRICS;
        private JvmMetricsCollector _poolMemoryMetricsCollector = DEFAULT_POOL_MEMORY_METRICS_COLLECTOR;
        private JvmMetricsCollector _heapMemoryMetricsCollector = DEFAULT_HEAP_MEMORY_METRICS_COLLECTOR;
        private JvmMetricsCollector _threadMetricsCollector = DEFAULT_THREAD_METRICS_COLLECTOR;
        private JvmMetricsCollector _garbageCollectionMetricsCollector = DEFAULT_GC_METRICS_COLLECTOR;
        private JvmMetricsCollector _bufferPoolMetricsCollector = DEFAULT_BUFFER_POOL_METRICS_COLLECTOR;
        private JvmMetricsCollector _fileDescriptorMetricsCollector = DEFAULT_FILE_DESCRIPTOR_METRICS_COLLECTOR;
        private JvmMetricsCollector _nonHeapMemoryMetricsCollector = DEFAULT_NON_HEAP_MEMORY_METRICS_COLLECTOR;

        private static final ManagementFactory DEFAULT_MANAGEMENT_FACTORY = ManagementFactoryDefault.newInstance();
        private static final Boolean DEFAULT_SWALLOW_EXCEPTION = true;
        private static final Boolean DEFAULT_COLLECT_NON_HEAP_MEMORY_METRICS = false;
        private static final Boolean DEFAULT_COLLECT_POOL_MEMORY_METRICS = true;
        private static final Boolean DEFAULT_COLLECT_HEAP_MEMORY_METRICS = true;
        private static final Boolean DEFAULT_COLLECT_THREAD_METRICS = true;
        private static final Boolean DEFAULT_COLLECT_GC_METRICS = true;
        private static final Boolean DEFAULT_COLLECT_BUFFER_POOL_METRICS = true;
        private static final Boolean DEFAULT_COLLECT_FILE_DESCRIPTOR_METRICS = true;
        private static final JvmMetricsCollector DEFAULT_POOL_MEMORY_METRICS_COLLECTOR =
                PoolMemoryMetricsCollector.newInstance();
        private static final JvmMetricsCollector DEFAULT_HEAP_MEMORY_METRICS_COLLECTOR =
                HeapMemoryMetricsCollector.newInstance();
        private static final JvmMetricsCollector DEFAULT_THREAD_METRICS_COLLECTOR =
                ThreadMetricsCollector.newInstance();
        private static final JvmMetricsCollector DEFAULT_GC_METRICS_COLLECTOR =
                GarbageCollectionMetricsCollector.newInstance();
        private static final JvmMetricsCollector DEFAULT_BUFFER_POOL_METRICS_COLLECTOR =
                BufferPoolMetricsCollector.newInstance();
        private static final JvmMetricsCollector DEFAULT_FILE_DESCRIPTOR_METRICS_COLLECTOR =
                FileDescriptorMetricsCollector.newInstance();
        @SuppressWarnings("deprecation")
        private static final JvmMetricsCollector DEFAULT_NON_HEAP_MEMORY_METRICS_COLLECTOR =
                com.arpnetworking.metrics.jvm.collectors.NonHeapMemoryMetricsCollector.newInstance();

    }

    /**
     * An implementation class of <code>ManagementFactory</code> that is to be
     * used for getting the actual values for jvm metrics from the java
     * management API. This class exists to facilitate testing only and the
     * clients should never have to explicitly instantiate this.
     *
     * @author Deepika Misra (deepika at groupon dot com)
     */
    /* package private */ static final class ManagementFactoryDefault implements ManagementFactory {

        /**
         * Creates a new instance of <code>ManagementFactoryDefault</code>.
         *
         * @return An instance of <code>ManagementFactoryDefault</code>
         */
        /* package private */
        static ManagementFactory newInstance() {
            return new ManagementFactoryDefault();
        }

        @Override
        public List<GarbageCollectorMXBean> getGarbageCollectorMXBeans() {
            return java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
        }

        @Override
        public MemoryMXBean getMemoryMXBean() {
            return java.lang.management.ManagementFactory.getMemoryMXBean();
        }

        @Override
        public List<MemoryPoolMXBean> getMemoryPoolMXBeans() {
            return java.lang.management.ManagementFactory.getMemoryPoolMXBeans();
        }

        @Override
        public ThreadMXBean getThreadMXBean() {
            return java.lang.management.ManagementFactory.getThreadMXBean();
        }

        @Override
        public List<BufferPoolMXBean> getBufferPoolMXBeans() {
            return java.lang.management.ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        }

        @Override
        public OperatingSystemMXBean getOperatingSystemMXBean() {
            return java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        }

        private ManagementFactoryDefault() {
        }
    }
}
