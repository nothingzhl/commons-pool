/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.pool2.impl;

import org.apache.commons.pool2.TrackedUse;
import org.apache.commons.pool2.UsageTracking;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.Duration;

/**
 * Configuration settings for abandoned object removal.
 *
 * @since 2.0
 */
public class AbandonedConfig {

    /**
     * The 5 minutes Duration.
     */
    private static final Duration DEFAULT_REMOVE_ABANDONED_TIMEOUT_DURATION = Duration.ofMinutes(5);
    /**
     * Whether or not borrowObject performs abandoned object removal.
     */
    private boolean removeAbandonedOnBorrow;
    /**
     * Whether or not pool maintenance (evictor) performs abandoned object
     * removal.
     * 对象池本省可以通过GenericObjectPoolConfig配置后台异步清理任务，但是后台清理任务的主要职责是关注空闲（状态为空闲）对象的检测和清理。
     * 如果removeAbandonedOnMaintenance设置为true，就意味着，在对象池的异步清理任务中，也可以进行遗弃（状态为活跃）对象的相关清理。
     */
    private boolean removeAbandonedOnMaintenance;
    /**
     * Timeout before an abandoned object can be removed.
     * 这个时间，默认5min。如果对象池的一个对象被占用了超过5min还没有被释放，就认为是被调用方遗弃了。
     */
    private Duration removeAbandonedTimeoutDuration = DEFAULT_REMOVE_ABANDONED_TIMEOUT_DURATION;
    /**
     * Determines whether or not to log stack traces for application code
     * which abandoned an object.
     * 如果处理发了遗弃对象的回收和清理，是否要打印该对象的调用堆栈。
     * 生产环境十分不建议设为true，会很消耗性能。
     */
    private boolean logAbandoned;
    /**
     * Determines whether or not to log full stack traces when logAbandoned is true.
     * If disabled, then a faster method for logging stack traces with only class data
     * may be used if possible.
     *
     * @since 2.5
     */
    private boolean requireFullStackTrace = true;
    /**
     * PrintWriter to use to log information on abandoned objects.
     * Use of default system encoding is deliberate.
     * 这是一个日志输出器，用来定义调用堆栈日志的输出行为（输出到控制台、文件等）。默认输出到控制台。
     * 如果logAbandoned为false，就不会有输出行为。
     */
    private PrintWriter logWriter = new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()));
    /**
     * If the pool implements {@link UsageTracking}, should the pool record a
     * stack trace every time a method is called on a pooled object and retain
     * the most recent stack trace to aid debugging of abandoned objects?
     * 这个UsageTracking从名字上来直观翻译，叫做使用追踪，指的就是池中对象的使用追踪。UsageTracking也是一个接口，GenericObjectPool实现了该接口。
     * 什么叫使用？
     * 就是只要是调用了池中对象的任何方法，就算使用了池中对象。
     * 什么叫追踪？
     * 就是在池中某个对象的任何一个方法被调用时，都会创建一个调用堆栈快照。
     * logAbandoned为true时，useUsageTracking也为true时，那么回收被遗弃的对象时，就会打印该对象最后一次的调用堆栈信息了。
     * 如果useUsageTracking为true，即便是logAbandoned为false，那么每次对象的方法调用，一样还是会创建调用堆栈对象。只不过最终被回收时不会打印输出。
     * 生产环境该属性也不建议设置为true。
     */
    private boolean useUsageTracking;

    /**
     * Creates a new instance.
     */
    public AbandonedConfig() {
        // empty
    }

    /**
     * Creates a new instance with values from the given instance.
     *
     * @param abandonedConfig the source.
     */
    @SuppressWarnings("resource")
    private AbandonedConfig(final AbandonedConfig abandonedConfig) {
        this.setLogAbandoned(abandonedConfig.getLogAbandoned());
        this.setLogWriter(abandonedConfig.getLogWriter());
        this.setRemoveAbandonedOnBorrow(abandonedConfig.getRemoveAbandonedOnBorrow());
        this.setRemoveAbandonedOnMaintenance(abandonedConfig.getRemoveAbandonedOnMaintenance());
        this.setRemoveAbandonedTimeout(abandonedConfig.getRemoveAbandonedTimeoutDuration());
        this.setUseUsageTracking(abandonedConfig.getUseUsageTracking());
        this.setRequireFullStackTrace(abandonedConfig.getRequireFullStackTrace());
    }

    /**
     * Creates a new instance with values from the given instance.
     *
     * @param abandonedConfig the source, may be null.
     * @return A new instance or null if the input is null.
     * @since 2.11.0
     */
    public static AbandonedConfig copy(final AbandonedConfig abandonedConfig) {
        return abandonedConfig == null ? null : new AbandonedConfig(abandonedConfig);
    }

    /**
     * Flag to log stack traces for application code which abandoned
     * an object.
     * <p>
     * Defaults to false.
     * Logging of abandoned objects adds overhead for every object created
     * because a stack trace has to be generated.
     *
     * @return boolean true if stack trace logging is turned on for abandoned
     * objects
     */
    public boolean getLogAbandoned() {
        return this.logAbandoned;
    }

    /**
     * Sets the flag to log stack traces for application code which abandoned
     * an object.
     *
     * @param logAbandoned true turns on abandoned stack trace logging
     * @see #getLogAbandoned()
     */
    public void setLogAbandoned(final boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    /**
     * Gets the log writer being used by this configuration to log
     * information on abandoned objects. If not set, a PrintWriter based on
     * System.out with the system default encoding is used.
     *
     * @return log writer in use
     */
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    /**
     * Sets the log writer to be used by this configuration to log
     * information on abandoned objects.
     *
     * @param logWriter The new log writer
     */
    public void setLogWriter(final PrintWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * <p>Flag to remove abandoned objects if they exceed the
     * removeAbandonedTimeout when borrowObject is invoked.</p>
     *
     * <p>The default value is false.</p>
     *
     * <p>If set to true, abandoned objects are removed by borrowObject if
     * there are fewer than 2 idle objects available in the pool and
     * {@code getNumActive() &gt; getMaxTotal() - 3}</p>
     *
     * @return true if abandoned objects are to be removed by borrowObject
     */
    public boolean getRemoveAbandonedOnBorrow() {
        return this.removeAbandonedOnBorrow;
    }

    /**
     * Flag to remove abandoned objects if they exceed the
     * removeAbandonedTimeout when borrowObject is invoked.
     *
     * @param removeAbandonedOnBorrow true means abandoned objects will be
     *                                removed by borrowObject
     * @see #getRemoveAbandonedOnBorrow()
     */
    public void setRemoveAbandonedOnBorrow(final boolean removeAbandonedOnBorrow) {
        this.removeAbandonedOnBorrow = removeAbandonedOnBorrow;
    }

    /**
     * <p>Flag to remove abandoned objects if they exceed the
     * removeAbandonedTimeout when pool maintenance (the "evictor")
     * runs.</p>
     *
     * <p>The default value is false.</p>
     *
     * <p>If set to true, abandoned objects are removed by the pool
     * maintenance thread when it runs.  This setting has no effect
     * unless maintenance is enabled by setting
     *{@link GenericObjectPool#getDurationBetweenEvictionRuns() durationBetweenEvictionRuns}
     * to a positive number.</p>
     *
     * @return true if abandoned objects are to be removed by the evictor
     */
    public boolean getRemoveAbandonedOnMaintenance() {
        return this.removeAbandonedOnMaintenance;
    }

    /**
     * Flag to remove abandoned objects if they exceed the
     * removeAbandonedTimeout when pool maintenance runs.
     *
     * @param removeAbandonedOnMaintenance true means abandoned objects will be
     *                                     removed by pool maintenance
     * @see #getRemoveAbandonedOnMaintenance
     */
    public void setRemoveAbandonedOnMaintenance(final boolean removeAbandonedOnMaintenance) {
        this.removeAbandonedOnMaintenance = removeAbandonedOnMaintenance;
    }

    /**
     * <p>Timeout in seconds before an abandoned object can be removed.</p>
     *
     * <p>The time of most recent use of an object is the maximum (latest) of
     * {@link TrackedUse#getLastUsedInstant()} (if this class of the object implements
     * TrackedUse) and the time when the object was borrowed from the pool.</p>
     *
     * <p>The default value is 300 seconds.</p>
     *
     * @return the abandoned object timeout in seconds.
     * @deprecated Use {@link #getRemoveAbandonedTimeoutDuration()}.
     */
    @Deprecated
    public int getRemoveAbandonedTimeout() {
        return (int) this.removeAbandonedTimeoutDuration.getSeconds();
    }

    /**
     * Sets the timeout before an abandoned object can be
     * removed.
     *
     * <p>Setting this property has no effect if
     * {@link #getRemoveAbandonedOnBorrow() removeAbandonedOnBorrow} and
     * {@link #getRemoveAbandonedOnMaintenance() removeAbandonedOnMaintenance}
     * are both false.</p>
     *
     * @param removeAbandonedTimeout new abandoned timeout
     * @see #getRemoveAbandonedTimeoutDuration()
     * @since 2.10.0
     */
    public void setRemoveAbandonedTimeout(final Duration removeAbandonedTimeout) {
        this.removeAbandonedTimeoutDuration = PoolImplUtils.nonNull(removeAbandonedTimeout, DEFAULT_REMOVE_ABANDONED_TIMEOUT_DURATION);
    }

    /**
     * Sets the timeout in seconds before an abandoned object can be
     * removed.
     *
     * <p>Setting this property has no effect if
     * {@link #getRemoveAbandonedOnBorrow() removeAbandonedOnBorrow} and
     * {@link #getRemoveAbandonedOnMaintenance() removeAbandonedOnMaintenance}
     * are both false.</p>
     *
     * @param removeAbandonedTimeoutSeconds new abandoned timeout in seconds
     * @see #getRemoveAbandonedTimeoutDuration()
     * @deprecated Use {@link #setRemoveAbandonedTimeout(Duration)}.
     */
    @Deprecated
    public void setRemoveAbandonedTimeout(final int removeAbandonedTimeoutSeconds) {
        setRemoveAbandonedTimeout(Duration.ofSeconds(removeAbandonedTimeoutSeconds));
    }

    /**
     * <p>Timeout before an abandoned object can be removed.</p>
     *
     * <p>The time of most recent use of an object is the maximum (latest) of
     * {@link TrackedUse#getLastUsedInstant()} (if this class of the object implements
     * TrackedUse) and the time when the object was borrowed from the pool.</p>
     *
     * <p>The default value is 300 seconds.</p>
     *
     * @return the abandoned object timeout.
     * @since 2.10.0
     */
    public Duration getRemoveAbandonedTimeoutDuration() {
        return this.removeAbandonedTimeoutDuration;
    }

    /**
     * Indicates if full stack traces are required when {@link #getLogAbandoned() logAbandoned}
     * is true. Defaults to true. Logging of abandoned objects requiring a full stack trace will
     * generate an entire stack trace to generate for every object created. If this is disabled,
     * a faster but less informative stack walking mechanism may be used if available.
     *
     * @return true if full stack traces are required for logging abandoned connections, or false
     * if abbreviated stack traces are acceptable
     * @see CallStack
     * @since 2.5
     */
    public boolean getRequireFullStackTrace() {
        return requireFullStackTrace;
    }

    /**
     * Sets the flag to require full stack traces for logging abandoned connections when enabled.
     *
     * @param requireFullStackTrace indicates whether or not full stack traces are required in
     *                              abandoned connection logs
     * @see CallStack
     * @see #getRequireFullStackTrace()
     * @since 2.5
     */
    public void setRequireFullStackTrace(final boolean requireFullStackTrace) {
        this.requireFullStackTrace = requireFullStackTrace;
    }

    /**
     * If the pool implements {@link UsageTracking}, should the pool record a
     * stack trace every time a method is called on a pooled object and retain
     * the most recent stack trace to aid debugging of abandoned objects?
     *
     * @return {@code true} if usage tracking is enabled
     */
    public boolean getUseUsageTracking() {
        return useUsageTracking;
    }

    /**
     * If the pool implements {@link UsageTracking}, configure whether the pool
     * should record a stack trace every time a method is called on a pooled
     * object and retain the most recent stack trace to aid debugging of
     * abandoned objects.
     *
     * @param useUsageTracking A value of {@code true} will enable
     *                         the recording of a stack trace on every use
     *                         of a pooled object
     */
    public void setUseUsageTracking(final boolean useUsageTracking) {
        this.useUsageTracking = useUsageTracking;
    }

    /**
     * @since 2.4.3
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("AbandonedConfig [removeAbandonedOnBorrow=");
        builder.append(removeAbandonedOnBorrow);
        builder.append(", removeAbandonedOnMaintenance=");
        builder.append(removeAbandonedOnMaintenance);
        builder.append(", removeAbandonedTimeoutDuration=");
        builder.append(removeAbandonedTimeoutDuration);
        builder.append(", logAbandoned=");
        builder.append(logAbandoned);
        builder.append(", logWriter=");
        builder.append(logWriter);
        builder.append(", useUsageTracking=");
        builder.append(useUsageTracking);
        builder.append("]");
        return builder.toString();
    }
}
