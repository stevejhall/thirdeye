package com.linkedin.thirdeye.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.linkedin.thirdeye.api.TimeSpec;

public abstract class BaseThirdEyeResponse implements ThirdEyeResponse {
  protected final List<MetricFunction> metricFunctions;
  protected final ThirdEyeRequest request;
  protected final TimeSpec dataTimeSpec;
  protected final List<String> groupKeyColumns;

  public BaseThirdEyeResponse(ThirdEyeRequest request, TimeSpec dataTimeSpec) {
    this.request = request;
    this.dataTimeSpec = dataTimeSpec;
    this.metricFunctions = request.getMetricFunctions();
    this.groupKeyColumns = new ArrayList<>();
    if (request.getGroupByTimeGranularity() != null) {
      groupKeyColumns.add(dataTimeSpec.getColumnName());
    }
    groupKeyColumns.addAll(request.getGroupBy());
  }

  @Override
  public List<MetricFunction> getMetricFunctions() {
    return metricFunctions;
  }

  @Override
  public abstract int getNumRowsFor(MetricFunction metricFunction);

  @Override
  public abstract Map<String, String> getRow(MetricFunction metricFunction, int rowId);

  @Override
  public ThirdEyeRequest getRequest() {
    return request;
  }

  @Override
  public TimeSpec getDataTimeSpec() {
    return dataTimeSpec;
  }

  @Override
  public List<String> getGroupKeyColumns() {
    return groupKeyColumns;
  }

  @Override
  public String toString() {
    return super.toString();
  }

}
