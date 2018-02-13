/**
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
import java.util.List;

/**
 * Collector class for JVM memory usage metrics for each non-heap memory pool. Uses the Java Management API to get the
 * metrics data.
 *
 * @deprecated Use {@link PoolMemoryMetricsCollector} instead.
 * @author Deepika Misra (deepika at groupon dot com)
*/
// CHECKSTYLE.OFF: FinalClass - Allow clients to inherit from this.
@Deprecated
public class NonHeapMemoryMetricsCollector extends PoolMemoryMetricsCollector {
// CHECKSTYLE.ON: FinalClass
    /**
     * Creates a new instance of <code>JvmMetricsCollector</code>.
     *
     * @return An instance of <code>JvmMetricsCollector</code>
     */
    public static JvmMetricsCollector newInstance() {
        return new NonHeapMemoryMetricsCollector();
    }

    @Override
    public void collect(final Metrics metrics, final ManagementFactory managementFactory) {
        final List<MemoryPoolMXBean> memoryPoolBeans = managementFactory.getMemoryPoolMXBeans();
        for (final MemoryPoolMXBean pool : memoryPoolBeans) {
            if (MemoryType.NON_HEAP.equals(pool.getType())) {
                recordMetricsForPool(pool, metrics);
            }
        }
    }

    /**
     * Protected constructor.
     */
    protected NonHeapMemoryMetricsCollector() {}
}
