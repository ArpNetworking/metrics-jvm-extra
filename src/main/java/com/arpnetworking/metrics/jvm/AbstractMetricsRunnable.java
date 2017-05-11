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

/**
 * Abstract metrics collection runnable implementation.
 *
 * @author Ville Koskela (ville dot koskela at inscopemetrics dot com)
 */
public abstract class AbstractMetricsRunnable implements Runnable {

    @Override
    public void run() {
        // TODO(ville): Convert to try-catch with resources.
        // See: https://github.com/jacoco/jacoco/issues/82
        // Would be:
        /*
        try (final Metrics metrics = _metricsFactory.create()) {
            collectMetrics(metrics);
            // CHECKSTYLE.OFF: IllegalCatch - No checked exceptions here
        } catch (final Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            handleException(e);
        }
        */
        // Versus this ugliness:
        Metrics metrics = null;
        try {
            metrics = _metricsFactory.create();
            collectMetrics(metrics);
            // CHECKSTYLE.OFF: IllegalCatch - No checked exceptions here
        } catch (final Exception e) {
            // CHECKSTYLE.ON: IllegalCatch
            handleException(e);
        } finally {
            try {
                metrics.close();
                // CHECKSTYLE.OFF: IllegalCatch - No checked exceptions here
            } catch (final Exception e) {
                // CHECKSTYLE.ON: IllegalCatch
                handleException(e);
            }
        }
    }

    /**
     * Collect metrics to the supplied {@code Metrics} instance.
     *
     * @param metrics this unit of work's {@code Metrics} instance
     */
    protected abstract void collectMetrics(final Metrics metrics);

    /**
     * Handles exceptions either by logging and swalling or by rethrowing as
     * {@code RuntimeException} depending on the value of {@code _swallowException}.
     *
     * @param e An instance of <code>Exception</code>.
     */
    protected void handleException(final Exception e) {
        if (_swallowException) {
            _logger.warn("Metrics collection failed.", e);
        } else {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Protected constructor.
     *
     * @param metricsFactory the {@code MetricsFactory} instance
     * @param swallowExceptions whether exceptions are swalled or thrown
     * @param logger the {@code Logger} instance
     */
    protected AbstractMetricsRunnable(
            final MetricsFactory metricsFactory,
            final boolean swallowExceptions,
            final Logger logger) {
        _metricsFactory = metricsFactory;
        _swallowException = swallowExceptions;
        _logger = logger;
    }

    private final MetricsFactory _metricsFactory;
    private final boolean _swallowException;
    private final Logger _logger;
}
