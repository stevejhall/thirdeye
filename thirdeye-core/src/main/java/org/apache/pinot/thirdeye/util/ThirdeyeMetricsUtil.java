/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.util;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

/**
 * Deprecated in favor of using injected {@link MetricRegistry}
 */
@Deprecated
public class ThirdeyeMetricsUtil {

  private static final MetricRegistry metricRegistry = new MetricRegistry();

  public static final Counter dataQualityTaskCounter =
      metricRegistry.counter("dataQualityTaskCounter");
  public static final Counter dataQualityTaskSuccessCounter =
      metricRegistry.counter("dataQualityTaskSuccessCounter");
  public static final Counter dataQualityTaskExceptionCounter =
      metricRegistry.counter("dataQualityTaskExceptionCounter");
  public static final Counter couchbaseCallCounter =
      metricRegistry.counter("couchbaseCallCounter");
  public static final Counter couchbaseWriteCounter =
      metricRegistry.counter("couchbaseWriteCounter");
  public static final Counter couchbaseExceptionCounter =
      metricRegistry.counter("couchbaseExceptionCounter");
  public static final Counter rcaPipelineCallCounter =
      metricRegistry.counter("rcaPipelineCallCounter");
  public static final Counter rcaPipelineDurationCounter =
      metricRegistry.counter("rcaPipelineDurationCounter");
  public static final Counter rcaPipelineExceptionCounter =
      metricRegistry.counter("rcaPipelineExceptionCounter");
  public static final Counter rcaFrameworkCallCounter =
      metricRegistry.counter("rcaFrameworkCallCounter");
  public static final Counter rcaFrameworkDurationCounter =
      metricRegistry.counter("rcaFrameworkDurationCounter");
  public static final Counter rcaFrameworkExceptionCounter =
      metricRegistry.counter("rcaFrameworkExceptionCounter");
  public static final Counter cubeCallCounter =
      metricRegistry.counter("cubeCallCounter");
  public static final Counter cubeDurationCounter =
      metricRegistry.counter("cubeDurationCounter");
  public static final Counter cubeExceptionCounter =
      metricRegistry.counter("cubeExceptionCounter");
  public static final Counter detectionRetuneCounter =
      metricRegistry.counter("detectionRetuneCounter");
  public static final Counter triggerEventCounter =
      metricRegistry.counter("triggerEventCounter");
  public static final Counter processedTriggerEventCounter =
      metricRegistry.counter("processedTriggerEventCounter");
  public static final Counter eventScheduledTaskCounter =
      metricRegistry.counter("eventScheduledTaskCounter");
  public static final Counter eventScheduledTaskFallbackCounter =
      metricRegistry.counter("eventScheduledTaskFallbackCounter");
  public static final Counter jiraAlertsSuccessCounter =
      metricRegistry.counter("jiraAlertsSuccessCounter");
  public static final Counter jiraAlertsFailedCounter =
      metricRegistry.counter("jiraAlertsFailedCounter");
  public static final Counter jiraAlertsNumTicketsCounter =
      metricRegistry.counter("jiraAlertsNumTicketsCounter");
  public static final Counter jiraAlertsNumCommentsCounter =
      metricRegistry.counter("jiraAlertsNumCommentsCounter");

  private ThirdeyeMetricsUtil() {
  }
}
