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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

/**
 * Tests the {@link ExecutorServiceMetricsRunnable} class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot io)
 */
public final class AbstractMetricsRunnableTest {

    @Mock
    private MetricsFactory _metricsFactory;
    @Mock
    private Metrics _metrics;
    @Mock
    private Logger _logger;
    private AutoCloseable _mocks;

    @Before
    public void startUp() {
        _mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void after() throws Exception {
        _mocks.close();
    }

    @Test
    public void testClose() {
        Mockito.doReturn(_metrics).when(_metricsFactory).create();

        new TestMetricsRunnable(_metricsFactory, _logger, true).run();
        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_logger).warn(Mockito.anyString(), Mockito.any(RuntimeException.class));
        Mockito.verify(_metrics).close();
    }

    @Test
    public void testMetricsFactoryReturnsNull() {
        Mockito.doReturn(null).when(_metricsFactory).create();

        new TestMetricsRunnable(_metricsFactory, _logger, true).run();
        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_logger, Mockito.never()).warn(Mockito.anyString(), Mockito.any(RuntimeException.class));
    }

    @Test
    public void testMetricsCloseThrows() {
        Mockito.doReturn(_metrics).when(_metricsFactory).create();
        Mockito.doThrow(new RuntimeException("Test Exception")).when(_metrics).close();

        new TestMetricsRunnable(_metricsFactory, _logger, false).run();
        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_logger).warn(Mockito.anyString(), Mockito.any(RuntimeException.class));
    }

    private static final class TestMetricsRunnable extends AbstractMetricsRunnable {

        /* package private */ TestMetricsRunnable(
                final MetricsFactory metricsFactory,
                final Logger logger,
                final boolean collectThrows) {
            super(metricsFactory, true, logger);
            _collectThrows = collectThrows;
        }

        @Override
        protected void collectMetrics(final Metrics metrics) {
            if (_collectThrows) {
                throw new RuntimeException("Collect throws!");
            }
        }

        private boolean _collectThrows;
    }
}
