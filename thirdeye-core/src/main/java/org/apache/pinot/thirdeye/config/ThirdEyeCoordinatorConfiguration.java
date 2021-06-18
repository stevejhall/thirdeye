package org.apache.pinot.thirdeye.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.anomaly.detection.trigger.utils.DataAvailabilitySchedulingConfiguration;
import org.apache.pinot.thirdeye.anomaly.task.TaskDriverConfiguration;
import org.apache.pinot.thirdeye.common.restclient.ThirdEyeRestClientConfiguration;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.detection.cache.CacheConfig;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;

public class ThirdEyeCoordinatorConfiguration extends Configuration {

  @JsonProperty("auth")
  private AuthConfiguration authConfiguration;

  @JsonProperty("database")
  private DatabaseConfiguration databaseConfiguration;

  @JsonProperty("swagger")
  private SwaggerBundleConfiguration swaggerBundleConfiguration;

  @JsonProperty("mockEvents")
  private MockEventsConfiguration mockEventsConfiguration = new MockEventsConfiguration();

  @JsonProperty("taskDriver")
  private TaskDriverConfiguration taskDriverConfiguration = new TaskDriverConfiguration();

  @JsonProperty("scheduler")
  private ThirdEyeSchedulerConfiguration schedulerConfiguration = new ThirdEyeSchedulerConfiguration();

  @JsonProperty("cache")
  private CacheConfig cacheConfig = new CacheConfig();

  @JsonProperty("rca")
  private RCAConfiguration rcaConfiguration = new RCAConfiguration();

  private String configPath = "config";
  private boolean schedulerEnabled = false;

  // TODO spyne refactor legacy configs.
  private Map<String, Map<String, Object>> alerterConfigurations;
  private String phantomJsPath = "";
  private String failureFromAddress;
  private String failureToAddress;
  private String dashboardHost;
  private ThirdEyeRestClientConfiguration teRestConfig = new ThirdEyeRestClientConfiguration();
  private DataAvailabilitySchedulingConfiguration
      dataAvailabilitySchedulingConfiguration = new DataAvailabilitySchedulingConfiguration();
  private List<String> holidayCountriesWhitelist;
  private String rootDir = "";

  public AuthConfiguration getAuthConfiguration() {
    return authConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setAuthConfiguration(
      final AuthConfiguration authConfiguration) {
    this.authConfiguration = authConfiguration;
    return this;
  }

  public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
    return swaggerBundleConfiguration;
  }

  public void setSwaggerBundleConfiguration(
      final SwaggerBundleConfiguration swaggerBundleConfiguration) {
    this.swaggerBundleConfiguration = swaggerBundleConfiguration;
  }

  public DatabaseConfiguration getDatabaseConfiguration() {
    return databaseConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setDatabaseConfiguration(
      final DatabaseConfiguration databaseConfiguration) {
    this.databaseConfiguration = databaseConfiguration;
    return this;
  }

  public String getConfigPath() {
    return configPath;
  }

  public ThirdEyeCoordinatorConfiguration setConfigPath(final String configPath) {
    this.configPath = configPath;
    return this;
  }

  public MockEventsConfiguration getMockEventsConfiguration() {
    return mockEventsConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setMockEventsConfiguration(
      final MockEventsConfiguration mockEventsConfiguration) {
    this.mockEventsConfiguration = mockEventsConfiguration;
    return this;
  }

  public boolean isSchedulerEnabled() {
    return schedulerEnabled;
  }

  public ThirdEyeCoordinatorConfiguration setSchedulerEnabled(final boolean schedulerEnabled) {
    this.schedulerEnabled = schedulerEnabled;
    return this;
  }

  public TaskDriverConfiguration getTaskDriverConfiguration() {
    return taskDriverConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setTaskDriverConfiguration(
      final TaskDriverConfiguration taskDriverConfiguration) {
    this.taskDriverConfiguration = taskDriverConfiguration;
    return this;
  }

  public Map<String, Map<String, Object>> getAlerterConfigurations() {
    return alerterConfigurations;
  }

  public ThirdEyeCoordinatorConfiguration setAlerterConfigurations(
      final Map<String, Map<String, Object>> alerterConfigurations) {
    this.alerterConfigurations = alerterConfigurations;
    return this;
  }

  public String getPhantomJsPath() {
    return phantomJsPath;
  }

  public ThirdEyeCoordinatorConfiguration setPhantomJsPath(final String phantomJsPath) {
    this.phantomJsPath = phantomJsPath;
    return this;
  }

  public String getFailureFromAddress() {
    return failureFromAddress;
  }

  public ThirdEyeCoordinatorConfiguration setFailureFromAddress(final String failureFromAddress) {
    this.failureFromAddress = failureFromAddress;
    return this;
  }

  public String getFailureToAddress() {
    return failureToAddress;
  }

  public ThirdEyeCoordinatorConfiguration setFailureToAddress(final String failureToAddress) {
    this.failureToAddress = failureToAddress;
    return this;
  }

  public String getDashboardHost() {
    return dashboardHost;
  }

  public ThirdEyeCoordinatorConfiguration setDashboardHost(final String dashboardHost) {
    this.dashboardHost = dashboardHost;
    return this;
  }

  public ThirdEyeRestClientConfiguration getTeRestConfig() {
    return teRestConfig;
  }

  public ThirdEyeCoordinatorConfiguration setTeRestConfig(
      final ThirdEyeRestClientConfiguration teRestConfig) {
    this.teRestConfig = teRestConfig;
    return this;
  }

  public DataAvailabilitySchedulingConfiguration getDataAvailabilitySchedulingConfiguration() {
    return dataAvailabilitySchedulingConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setDataAvailabilitySchedulingConfiguration(
      final DataAvailabilitySchedulingConfiguration dataAvailabilitySchedulingConfiguration) {
    this.dataAvailabilitySchedulingConfiguration = dataAvailabilitySchedulingConfiguration;
    return this;
  }

  public List<String> getHolidayCountriesWhitelist() {
    return holidayCountriesWhitelist;
  }

  public ThirdEyeCoordinatorConfiguration setHolidayCountriesWhitelist(
      final List<String> holidayCountriesWhitelist) {
    this.holidayCountriesWhitelist = holidayCountriesWhitelist;
    return this;
  }

  public String getRootDir() {
    return rootDir;
  }

  public ThirdEyeCoordinatorConfiguration setRootDir(final String rootDir) {
    this.rootDir = rootDir;
    return this;
  }

  public ThirdEyeSchedulerConfiguration getSchedulerConfiguration() {
    return schedulerConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setSchedulerConfiguration(
      final ThirdEyeSchedulerConfiguration schedulerConfiguration) {
    this.schedulerConfiguration = schedulerConfiguration;
    return this;
  }

  public CacheConfig getCacheConfig() {
    return cacheConfig;
  }

  public ThirdEyeCoordinatorConfiguration setCacheConfig(
      final CacheConfig cacheConfig) {
    this.cacheConfig = cacheConfig;
    return this;
  }

  public RCAConfiguration getRcaConfiguration() {
    return rcaConfiguration;
  }

  public ThirdEyeCoordinatorConfiguration setRcaConfiguration(
      final RCAConfiguration rcaConfiguration) {
    this.rcaConfiguration = rcaConfiguration;
    return this;
  }
}
