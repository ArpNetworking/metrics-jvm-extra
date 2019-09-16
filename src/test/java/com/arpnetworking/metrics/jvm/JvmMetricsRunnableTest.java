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
import com.arpnetworking.metrics.jvm.collectors.JvmMetricsCollector;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests the {@link JvmMetricsRunnable} class.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
@SuppressFBWarnings("NP_NONNULL_PARAM_VIOLATION")
public final class JvmMetricsRunnableTest {

    @Before
    public void setUp() {
        _metricsFactory = Mockito.mock(MetricsFactory.class);
        _metrics = Mockito.mock(Metrics.class);
        _managementFactory = Mockito.mock(ManagementFactory.class);
        _gcCollector = Mockito.mock(JvmMetricsCollector.class);
        _heapMemoryCollector = Mockito.mock(JvmMetricsCollector.class);
        _poolMemoryCollector = Mockito.mock(JvmMetricsCollector.class);
        _threadCollector = Mockito.mock(JvmMetricsCollector.class);
        _bufferPoolCollector = Mockito.mock(BufferPoolMetricsCollector.class);
        _fileDescriptorCollector = Mockito.mock(FileDescriptorMetricsCollector.class);
        Mockito.doReturn(_metrics).when(_metricsFactory).create();
    }

    @After
    public void tearDown() {
        _gcCollector = null;
        _heapMemoryCollector = null;
        _threadCollector = null;
        _bufferPoolCollector = null;
        _fileDescriptorCollector = null;
        _poolMemoryCollector = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateRunnableMetricsFactoryNullCase() {
        createJvmMetricsRunnableBuilder().setMetricsFactory(null).build();
    }

    @Test
    public void testCreateRunnableSwallowExceptionNullToDefault() {
        createJvmMetricsRunnableBuilder().setSwallowException(null).build();
    }

    @Test
    public void testCreateRunnableCollectHeapMemoryMetricsNullToDefault() {
        createJvmMetricsRunnableBuilder().setCollectHeapMemoryMetrics(null).build();
    }

    @Test
    public void testCreateRunnableCollectPoolMemoryMetricsNullToDefault() {
        createJvmMetricsRunnableBuilder().setCollectPoolMemoryMetrics(null).build();
    }

    @Test
    public void testCreateRunnableCollectThreadMetricsNullToDefault() {
        createJvmMetricsRunnableBuilder().setCollectThreadMetrics(null).build();
    }

    @Test
    public void testCreateRunnableCollectBufferPoolMetricsNullToDefault() {
        createJvmMetricsRunnableBuilder().setCollectBufferPoolMetrics(null).build();
    }

    @Test
    public void testCreateRunnableCollectGcMetricsNullToDefault() {
        createJvmMetricsRunnableBuilder().setCollectGarbageCollectionMetrics(null).build();
    }

    @Test
    public void testCreateRunnableCollectFileDescriptorNullToDefault() {
        createJvmMetricsRunnableBuilder().setCollectFileDescriptorMetrics(null).build();
    }

    @Test
    public void testCreateRunnableManagementFactoryNullToDefault() {
        createJvmMetricsRunnableBuilder().setManagementFactory(null).build();
    }

    @Test
    public void testCreateRunnablePoolMemoryMetricsCollectorNullToDefault() {
        createJvmMetricsRunnableBuilder().setPoolMemoryMetricsCollector(null).build();
    }

    @Test
    public void testCreateRunnableHeapMemoryMetricsCollectorNullToDefault() {
        createJvmMetricsRunnableBuilder().setHeapMemoryMetricsCollector(null).build();
    }

    @Test
    public void testCreateRunnableThreadyMetricsCollectorNullToDefault() {
        createJvmMetricsRunnableBuilder().setThreadMetricsCollector(null).build();
    }

    @Test
    public void testCreateRunnableGarbageCollectionMetricsCollectorNullToDefault() {
        createJvmMetricsRunnableBuilder().setGarbageCollectionMetricsCollector(null).build();
    }

    @Test
    public void testCreateRunnableBufferPoolMetricsCollectorNullToDefault() {
        createJvmMetricsRunnableBuilder().setBufferPoolMetricsCollector(null).build();
    }

    @Test
    public void testCreateRunnableFileDescriptorMetricsCollectorNullToDefault() {
        createJvmMetricsRunnableBuilder().setFileDescriptorMetricsCollector(null).build();
    }

    @Test
    public void testRunDefaultCollectorsEnabledCase() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().build();
        runnable.run();
        Mockito.verify(_gcCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verify(_heapMemoryCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verify(_poolMemoryCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verify(_threadCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verify(_bufferPoolCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verify(_fileDescriptorCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
    }

    @Test
    public void testRunCollectorsAllDisabledCase() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectGarbageCollectionMetrics(false)
                .setCollectHeapMemoryMetrics(false)
                .setCollectPoolMemoryMetrics(false)
                .setCollectThreadMetrics(false)
                .setCollectBufferPoolMetrics(false)
                .setCollectFileDescriptorMetrics(false)
                .build();
        runnable.run();
        Mockito.verifyZeroInteractions(_gcCollector);
        Mockito.verifyZeroInteractions(_heapMemoryCollector);
        Mockito.verifyZeroInteractions(_poolMemoryCollector);
        Mockito.verifyZeroInteractions(_threadCollector);
        Mockito.verifyZeroInteractions(_bufferPoolCollector);
        Mockito.verifyZeroInteractions(_fileDescriptorCollector);
    }

    @Test
    public void testRunOnlyGcCollectorEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectHeapMemoryMetrics(false)
                .setCollectPoolMemoryMetrics(false)
                .setCollectThreadMetrics(false)
                .setCollectBufferPoolMetrics(false)
                .setCollectFileDescriptorMetrics(false)
                .build();
        runnable.run();
        Mockito.verify(_gcCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verifyZeroInteractions(_heapMemoryCollector);
        Mockito.verifyZeroInteractions(_poolMemoryCollector);
        Mockito.verifyZeroInteractions(_threadCollector);
        Mockito.verifyZeroInteractions(_bufferPoolCollector);
        Mockito.verifyZeroInteractions(_fileDescriptorCollector);
    }

    @Test
    public void testRunOnlyHeapMemoryCollectorEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectGarbageCollectionMetrics(false)
                .setCollectPoolMemoryMetrics(false)
                .setCollectThreadMetrics(false)
                .setCollectBufferPoolMetrics(false)
                .setCollectFileDescriptorMetrics(false)
                .build();
        runnable.run();
        Mockito.verifyZeroInteractions(_gcCollector);
        Mockito.verify(_heapMemoryCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verifyZeroInteractions(_poolMemoryCollector);
        Mockito.verifyZeroInteractions(_threadCollector);
        Mockito.verifyZeroInteractions(_bufferPoolCollector);
        Mockito.verifyZeroInteractions(_fileDescriptorCollector);
    }

    @Test
    public void testRunOnlyPoolMemoryCollectorEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectGarbageCollectionMetrics(false)
                .setCollectPoolMemoryMetrics(true)
                .setCollectHeapMemoryMetrics(false)
                .setCollectThreadMetrics(false)
                .setCollectBufferPoolMetrics(false)
                .setCollectFileDescriptorMetrics(false)
                .build();
        runnable.run();
        Mockito.verifyZeroInteractions(_gcCollector);
        Mockito.verifyZeroInteractions(_heapMemoryCollector);
        Mockito.verify(_poolMemoryCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verifyZeroInteractions(_threadCollector);
        Mockito.verifyZeroInteractions(_bufferPoolCollector);
        Mockito.verifyZeroInteractions(_fileDescriptorCollector);
    }

    @Test
    public void testRunOnlyThreadCollectorEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectGarbageCollectionMetrics(false)
                .setCollectHeapMemoryMetrics(false)
                .setCollectPoolMemoryMetrics(false)
                .setCollectBufferPoolMetrics(false)
                .setCollectFileDescriptorMetrics(false)
                .build();
        runnable.run();
        Mockito.verifyZeroInteractions(_gcCollector);
        Mockito.verifyZeroInteractions(_heapMemoryCollector);
        Mockito.verifyZeroInteractions(_poolMemoryCollector);
        Mockito.verify(_threadCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verifyZeroInteractions(_bufferPoolCollector);
        Mockito.verifyZeroInteractions(_fileDescriptorCollector);
    }

    @Test
    public void testRunOnlyBufferPoolCollectorEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectGarbageCollectionMetrics(false)
                .setCollectHeapMemoryMetrics(false)
                .setCollectPoolMemoryMetrics(false)
                .setCollectThreadMetrics(false)
                .setCollectFileDescriptorMetrics(false)
                .build();
        runnable.run();
        Mockito.verifyZeroInteractions(_gcCollector);
        Mockito.verifyZeroInteractions(_heapMemoryCollector);
        Mockito.verifyZeroInteractions(_poolMemoryCollector);
        Mockito.verifyZeroInteractions(_threadCollector);
        Mockito.verify(_bufferPoolCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
        Mockito.verifyZeroInteractions(_fileDescriptorCollector);
    }

    @Test
    public void testRunOnlyFileDescriptorCollectorEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setCollectGarbageCollectionMetrics(false)
                .setCollectHeapMemoryMetrics(false)
                .setCollectPoolMemoryMetrics(false)
                .setCollectThreadMetrics(false)
                .setCollectBufferPoolMetrics(false)
                .build();
        runnable.run();
        Mockito.verifyZeroInteractions(_gcCollector);
        Mockito.verifyZeroInteractions(_heapMemoryCollector);
        Mockito.verifyZeroInteractions(_poolMemoryCollector);
        Mockito.verifyZeroInteractions(_threadCollector);
        Mockito.verifyZeroInteractions(_bufferPoolCollector);
        Mockito.verify(_fileDescriptorCollector).collect(Mockito.any(Metrics.class), Mockito.any(ManagementFactory.class));
    }

    @Test
    public void testRunWithExceptionOnGcCollect() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().build();
        Mockito.doThrow(RuntimeException.class).when(_gcCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test
    public void testRunWithExceptionOnHeapMemoryCollect() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().build();
        Mockito.doThrow(RuntimeException.class).when(_heapMemoryCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test
    public void testRunWithExceptionOnPoolMemoryCollect() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().build();
        Mockito.doThrow(RuntimeException.class).when(_poolMemoryCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test
    public void testRunWithExceptionOnBufferPoolCollect() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().build();
        Mockito.doThrow(RuntimeException.class).when(_bufferPoolCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test
    public void testRunWithExceptionOnFileDescriptorCollect() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().build();
        Mockito.doThrow(RuntimeException.class).when(_fileDescriptorCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test
    public void testRunWithExceptionThrownWithSwallowExceptionEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder().setSwallowException(true).build();
        Mockito.doThrow(RuntimeException.class).when(_threadCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test(expected = Exception.class)
    public void testRunWithExceptionThrownWithSwallowExceptionDisabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(false)
                .build();
        Mockito.doThrow(Exception.class).when(_threadCollector).collect(_metrics, _managementFactory);
        runnable.run();
    }

    @Test(expected = Exception.class)
    public void testRunWithExceptionOnMetricsCreateWithSwallowExceptionDisabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(false)
                .build();
        Mockito.doThrow(Exception.class).when(_metricsFactory).create();
        runnable.run();
    }

    @Test
    public void testRunWithExceptionOnMetricsCreateWithSwallowExceptionEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(true)
                .build();
        Mockito.doThrow(RuntimeException.class).when(_metricsFactory).create();
        runnable.run();
    }

    @Test(expected = Exception.class)
    public void testRunWithExceptionOnCollectAndCloseWithSwallowExceptionDisabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(false)
                .build();
        Mockito.doThrow(Exception.class).when(_gcCollector).collect(_metrics, _managementFactory);
        Mockito.doThrow(Exception.class).when(_metrics).close();
        runnable.run();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleRuntimeExceptionWithSwallowExceptionDisabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(false)
                .build();
        runnable.handleException(new IllegalArgumentException());
    }

    @Test(expected = RuntimeException.class)
    public void testHandleExceptionWithSwallowExceptionDisabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(false)
                .build();
        try {
            runnable.handleException(new ClassNotFoundException());
        // CHECKSTYLE.OFF: IllegalCatch - No checked exceptions thrown
        } catch (final Exception e) {
        // CHECKSTYLE.ON: IllegalCatch
            Assert.assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            throw e;
        }
    }

    @Test
    public void testHandleExceptionWithSwallowExceptionEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(true)
                .build();
        runnable.handleException(new ClassNotFoundException());
    }

    @Test
    public void testHandleRuntimeExceptionWithSwallowExceptionEnabled() {
        final JvmMetricsRunnable runnable = createJvmMetricsRunnableBuilder()
                .setSwallowException(true)
                .build();
        runnable.handleException(new RuntimeException());
    }
    
    private JvmMetricsRunnable.Builder createJvmMetricsRunnableBuilder() {
        return new JvmMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setManagementFactory(_managementFactory)
                .setGarbageCollectionMetricsCollector(_gcCollector)
                .setHeapMemoryMetricsCollector(_heapMemoryCollector)
                .setPoolMemoryMetricsCollector(_poolMemoryCollector)
                .setThreadMetricsCollector(_threadCollector)
                .setBufferPoolMetricsCollector(_bufferPoolCollector)
                .setFileDescriptorMetricsCollector(_fileDescriptorCollector);
    }

    private MetricsFactory _metricsFactory = null;
    private Metrics _metrics = null;
    private ManagementFactory _managementFactory = null;
    private JvmMetricsCollector _gcCollector = null;
    private JvmMetricsCollector _heapMemoryCollector = null;
    private JvmMetricsCollector _poolMemoryCollector = null;
    private JvmMetricsCollector _threadCollector = null;
    private JvmMetricsCollector _bufferPoolCollector = null;
    private JvmMetricsCollector _fileDescriptorCollector = null;
}
