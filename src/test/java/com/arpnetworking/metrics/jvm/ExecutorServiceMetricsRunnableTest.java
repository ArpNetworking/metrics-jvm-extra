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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Tests the <code>ExecutorServiceMetricsRunnable</code> class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class ExecutorServiceMetricsRunnableTest {

    @Mock
    private MetricsFactory _metricsFactory;
    @Mock
    private Metrics _metrics;

    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testThreadPoolCollection() {
        final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                1,
                10,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>());
        final Runnable runnable = new ExecutorServiceMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setExecutorServices(Collections.singletonMap("thread_pool", threadPool))
                .build();

        final ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Long> valueCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verifyZeroInteractions(_metricsFactory);
        Mockito.verifyZeroInteractions(_metrics);

        Mockito.doReturn(_metrics).when(_metricsFactory).create();

        runnable.run();

        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_metrics, Mockito.times(5)).setGauge(nameCaptor.capture(), valueCaptor.capture());
        Mockito.verify(_metrics).close();

        Assert.assertEquals("Capture mismatch", 5, nameCaptor.getAllValues().size());
        Assert.assertEquals("Capture mismatch", 5, valueCaptor.getAllValues().size());
        for (int i = 0; i < nameCaptor.getAllValues().size(); ++i) {
            final String name = nameCaptor.getAllValues().get(i);
            final long value = valueCaptor.getAllValues().get(i);
            if ("executor_services/thread_pool/active_threads".equals(name)) {
                Assert.assertEquals(0, value);
            } else if ("executor_services/thread_pool/queued_tasks".equals(name)) {
                Assert.assertEquals(0, value);
            } else if ("executor_services/thread_pool/completed_tasks".equals(name)) {
                Assert.assertEquals(0, value);
            } else if ("executor_services/thread_pool/thread_pool_maximum_size".equals(name)) {
                Assert.assertEquals(10, value);
            } else if ("executor_services/thread_pool/thread_pool_size".equals(name)) {
                Assert.assertEquals(0, value);
            } else {
                Assert.fail("Unexpected metric name: " + name);
            }
        }
    }

    @Test
    public void testForkJoinPoolCollection() {
        final ForkJoinPool forkJoin = new ForkJoinPool(3);
        final Runnable runnable = new ExecutorServiceMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setExecutorServices(Collections.singletonMap("fork_join_pool", forkJoin))
                .build();

        final ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Long> valueCaptor = ArgumentCaptor.forClass(Long.class);

        Mockito.verifyZeroInteractions(_metricsFactory);
        Mockito.verifyZeroInteractions(_metrics);

        Mockito.doReturn(_metrics).when(_metricsFactory).create();

        runnable.run();

        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_metrics, Mockito.times(5)).setGauge(nameCaptor.capture(), valueCaptor.capture());
        Mockito.verify(_metrics).close();

        Assert.assertEquals("Capture mismatch", 5, nameCaptor.getAllValues().size());
        Assert.assertEquals("Capture mismatch", 5, valueCaptor.getAllValues().size());
        for (int i = 0; i < nameCaptor.getAllValues().size(); ++i) {
            final String name = nameCaptor.getAllValues().get(i);
            final long value = valueCaptor.getAllValues().get(i);
            if ("executor_services/fork_join_pool/active_threads".equals(name)) {
                Assert.assertEquals(0, value);
            } else if ("executor_services/fork_join_pool/queued_submissions".equals(name)) {
                Assert.assertEquals(0, value);
            } else if ("executor_services/fork_join_pool/queued_tasks".equals(name)) {
                Assert.assertEquals(0, value);
            } else if ("executor_services/fork_join_pool/parallelism".equals(name)) {
                Assert.assertEquals(3, value);
            } else if ("executor_services/fork_join_pool/thread_pool_size".equals(name)) {
                Assert.assertEquals(0, value);
            } else {
                Assert.fail("Unexpected metric name: " + name);
            }
        }
    }

    @Test
    public void testBuild() {
        // CHECKSTYLE.OFF: IllegalInstantiation - No Guava here
        final Map<String, ExecutorService> executorServices = new HashMap<>();
        // CHECKSTYLE.ON: IllegalInstantiation
        executorServices.put(
                "thread_pool",
                new ThreadPoolExecutor(
                        1,
                        10,
                        60,
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<>()
                ));
        executorServices.put(
                "fork_join_pool",
                new ForkJoinPool(3));

        new ExecutorServiceMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setExecutorServices(executorServices)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderMetricsFactoryNull() {
        new ExecutorServiceMetricsRunnable.Builder()
                .setExecutorServices(Collections.emptyMap())
                .setSwallowException(false)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderExecutorServicesNull() {
        new ExecutorServiceMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setSwallowException(false)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderSwallowExceptionsNull() {
        new ExecutorServiceMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setExecutorServices(Collections.emptyMap())
                .setSwallowException(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderInvalidExecutorService() {
        new ExecutorServiceMetricsRunnable.Builder()
                .setMetricsFactory(_metricsFactory)
                .setExecutorServices(Collections.singletonMap("invalid", Mockito.mock(ExecutorService.class)))
                .build();
    }
}
