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
import com.arpnetworking.metrics.Units;
import com.arpnetworking.metrics.jvm.ManagementFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests <code>MemoryMetricsCollector</code> class.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
@SuppressWarnings("deprecation")
public final class NonHeapMemoryMetricsCollectorTest {

    @Before
    public void setUp() {
        _metrics =  Mockito.mock(Metrics.class);
        _managementFactory = Mockito.mock(ManagementFactory.class);
        _memoryPoolMXBean1 = Mockito.mock(MemoryPoolMXBean.class);
        _memoryPoolMXBean2 = Mockito.mock(MemoryPoolMXBean.class);
        _memoryPoolMXBean3 = Mockito.mock(MemoryPoolMXBean.class);
    }

    @After
    public void tearDown() {
        _metrics = null;
        _managementFactory = null;
        _memoryPoolMXBean1 = null;
        _memoryPoolMXBean2 = null;
        _memoryPoolMXBean3 = null;
    }

    @Test
    public void testCollectWithNoPoolBeans() {
        Mockito.doReturn(Collections.emptyList()).when(_managementFactory).getMemoryPoolMXBeans();
        NonHeapMemoryMetricsCollector.newInstance().collect(_metrics, _managementFactory);
        Mockito.verifyNoMoreInteractions(_metrics);
    }

    @Test
    public void testCollectWithMultipleBeans() {
        createMockBean(_memoryPoolMXBean1, "My Bean 1", 10L, 100L, MemoryType.NON_HEAP);
        createMockBean(_memoryPoolMXBean2, "My Bean 2", 20L, 300L, MemoryType.HEAP);
        createMockBean(_memoryPoolMXBean3, "My Bean 3", 30L, 400L, MemoryType.NON_HEAP);
        Mockito.doReturn(Arrays.asList(_memoryPoolMXBean1, _memoryPoolMXBean2, _memoryPoolMXBean3))
                .when(_managementFactory)
                .getMemoryPoolMXBeans();
        NonHeapMemoryMetricsCollector.newInstance().collect(_metrics, _managementFactory);
        Mockito.verify(_metrics).setGauge("jvm/non_heap_memory/my_bean_1/used", 10L, Units.BYTE);
        Mockito.verify(_metrics).setGauge("jvm/non_heap_memory/my_bean_1/max", 100L, Units.BYTE);
        Mockito.verify(_metrics).setGauge("jvm/non_heap_memory/my_bean_3/used", 30L, Units.BYTE);
        Mockito.verify(_metrics).setGauge("jvm/non_heap_memory/my_bean_3/max", 400L, Units.BYTE);
        Mockito.verifyNoMoreInteractions(_metrics);
    }

    private MemoryUsage createMockBean(
            final MemoryPoolMXBean pool,
            final String name,
            final long used,
            final long max,
            final MemoryType memoryType) {
        final MemoryUsage usage = Mockito.mock(MemoryUsage.class);
        Mockito.doReturn(used).when(usage).getUsed();
        Mockito.doReturn(max).when(usage).getMax();
        Mockito.doReturn(usage).when(pool).getUsage();
        Mockito.doReturn(name).when(pool).getName();
        Mockito.doReturn(memoryType).when(pool).getType();
        return usage;
    }

    private Metrics _metrics = null;
    private ManagementFactory _managementFactory = null;
    private MemoryPoolMXBean _memoryPoolMXBean1 = null;
    private MemoryPoolMXBean _memoryPoolMXBean2 = null;
    private MemoryPoolMXBean _memoryPoolMXBean3 = null;
}
