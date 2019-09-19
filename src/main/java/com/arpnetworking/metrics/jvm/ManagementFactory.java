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

import java.lang.management.BufferPoolMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * This interface defines the various methods to get JVM related data. This interface exists only to facilitate
 * testing and the clients should never need to implement this.
 *
 * @author Deepika Misra (deepika at groupon dot com)
 */
public interface ManagementFactory {

    /**
     * Gets the {@link List} of {@link GarbageCollectorMXBean}.
     *
     * @return A {@link List} of {@link GarbageCollectorMXBean}.
     */
    List<GarbageCollectorMXBean> getGarbageCollectorMXBeans();

    /**
     * Gets the {@link MemoryMXBean}.
     *
     * @return An instance of {@link MemoryMXBean}.
     */
    MemoryMXBean getMemoryMXBean();

    /**
     * Gets the {@link List} of {@link MemoryPoolMXBean}.
     *
     * @return A {@link List} of {@link MemoryPoolMXBean}.
     */
    List<MemoryPoolMXBean> getMemoryPoolMXBeans();

    /**
     * Gets the {@link ThreadMXBean}.
     *
     * @return An instance of {@link ThreadMXBean}.
     */
    ThreadMXBean getThreadMXBean();

    /**
     * Gets the {@link List} of {@link BufferPoolMXBean}.
     *
     * @return A {@link List} of {@link BufferPoolMXBean}.
     */
    List<BufferPoolMXBean> getBufferPoolMXBeans();

    /**
     * Gets the {@link OperatingSystemMXBean}.
     *
     * @return An instance of {@link OperatingSystemMXBean}.
     */
    OperatingSystemMXBean getOperatingSystemMXBean();
}



