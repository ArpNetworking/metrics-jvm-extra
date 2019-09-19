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
package com.arpnetworking.metrics.jvm.collectors;

import com.arpnetworking.metrics.Metrics;
import com.arpnetworking.metrics.jvm.ManagementFactory;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.List;

/**
 * Collector class for JVM memory usage metrics for each memory pool. Uses the Java Management API to get the metrics
 * data.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 * @author Brandon Arp (brandon dot arp at inscopemetrics dot io)
*/
// CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
public class PoolMemoryMetricsCollector implements JvmMetricsCollector {
// CHECKSTYLE.ON: FinalClass
    /**
     * Creates a new instance of {@link JvmMetricsCollector}.
     *
     * @return An instance of {@link JvmMetricsCollector}
     */
    public static JvmMetricsCollector newInstance() {
        return new PoolMemoryMetricsCollector();
    }

    @Override
    public void collect(final Metrics metrics, final ManagementFactory managementFactory) {
        final List<MemoryPoolMXBean> memoryPoolBeans = managementFactory.getMemoryPoolMXBeans();
        for (final MemoryPoolMXBean pool : memoryPoolBeans) {
            recordMetricsForPool(pool, metrics);
        }
    }

    /**
     * Records the metrics for a given pool.  Useful if a deriving class filters the list of pools to collect.
     *
     * @param pool {@link MemoryPoolMXBean} to record
     * @param metrics {@link Metrics} to record into
     */
    protected void recordMetricsForPool(final MemoryPoolMXBean pool, final Metrics metrics) {
        final MemoryUsage usage = pool.getUsage();
        metrics.setGauge(
                String.join(
                        "/",
                        ROOT_NAMESPACE,
                        memoryTypeSegment(pool.getType()),
                        MetricsUtil.convertToSnakeCase(pool.getName()),
                        MEMORY_USED),
                usage.getUsed()
        );
        final long memoryMax = usage.getMax();
        if (memoryMax != -1) {
            metrics.setGauge(
                    String.join(
                            "/",
                            ROOT_NAMESPACE,
                            memoryTypeSegment(pool.getType()),
                            MetricsUtil.convertToSnakeCase(pool.getName()),
                            MEMORY_MAX),
                    memoryMax
            );
        }
    }

    private String memoryTypeSegment(final MemoryType type) {
        if (MemoryType.HEAP.equals(type)) {
            return HEAP_MEMORY;
        } else {
            return NON_HEAP_MEMORY;
        }
    }

    /**
     * Protected constructor.
     */
    protected PoolMemoryMetricsCollector() {}

    private static final String MEMORY_USED = "used";
    private static final String MEMORY_MAX = "max";
    private static final String NON_HEAP_MEMORY = "non_heap_memory";
    private static final String HEAP_MEMORY = "heap_memory";
}
