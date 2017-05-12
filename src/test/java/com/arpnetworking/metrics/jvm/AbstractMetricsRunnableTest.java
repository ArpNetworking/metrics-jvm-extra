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
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

/**
 * Tests the <code>ExecutorServiceMetricsRunnable</code> class.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public final class AbstractMetricsRunnableTest {

    @Mock
    private MetricsFactory _metricsFactory;
    @Mock
    private Metrics _metrics;
    @Mock
    private Logger _logger;

    @Before
    public void startUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testClose() {
        Mockito.doReturn(_metrics).when(_metricsFactory).create();

        new TestMetricsRunnable(_metricsFactory, _logger, true).run();
        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_logger).warn(Matchers.anyString(), Matchers.any(RuntimeException.class));
        Mockito.verify(_metrics).close();
    }

    @Test
    public void testMetricsFactoryReturnsNull() {
        Mockito.doReturn(null).when(_metricsFactory).create();

        new TestMetricsRunnable(_metricsFactory, _logger, true).run();
        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_logger, Mockito.never()).warn(Matchers.anyString(), Matchers.any(RuntimeException.class));
    }

    @Test
    public void testMetricsCloseThrows() {
        Mockito.doReturn(_metrics).when(_metricsFactory).create();
        Mockito.doThrow(new RuntimeException("Test Exception")).when(_metrics).close();

        new TestMetricsRunnable(_metricsFactory, _logger, false).run();
        Mockito.verify(_metricsFactory).create();
        Mockito.verify(_logger).warn(Matchers.anyString(), Matchers.any(RuntimeException.class));
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
