/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.OnboardDatasetMetricManager;
import ai.startree.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class OnboardDatasetMetricManagerImpl extends AbstractManagerImpl<OnboardDatasetMetricDTO>
    implements OnboardDatasetMetricManager {

  @Inject
  public OnboardDatasetMetricManagerImpl(GenericPojoDao genericPojoDao) {
    super(OnboardDatasetMetricDTO.class, genericPojoDao);
  }

  @Override
  public List<OnboardDatasetMetricDTO> findByDataSource(String dataSource) {
    Predicate dataSourcePredicate = Predicate.EQ("dataSource", dataSource);
    return findByPredicate(dataSourcePredicate);
  }

  @Override
  public List<OnboardDatasetMetricDTO> findByDataSourceAndOnboarded(String dataSource,
      boolean onboarded) {
    Predicate dataSourcePredicate = Predicate.EQ("dataSource", dataSource);
    Predicate onboardedPredicate = Predicate.EQ("onboarded", onboarded);
    Predicate predicate = Predicate.AND(dataSourcePredicate, onboardedPredicate);
    return findByPredicate(predicate);
  }

  @Override
  public List<OnboardDatasetMetricDTO> findByDataset(String datasetName) {
    Predicate predicate = Predicate.EQ("datasetName", datasetName);
    return findByPredicate(predicate);
  }

  @Override
  public List<OnboardDatasetMetricDTO> findByMetric(String metricName) {
    Predicate predicate = Predicate.EQ("metricName", metricName);
    return findByPredicate(predicate);
  }

  @Override
  public List<OnboardDatasetMetricDTO> findByDatasetAndDatasource(String datasetName,
      String dataSource) {
    Predicate datasetNamePredicate = Predicate.EQ("datasetName", datasetName);
    Predicate dataSourcePredicate = Predicate.EQ("dataSource", dataSource);
    Predicate predicate = Predicate.AND(datasetNamePredicate, dataSourcePredicate);
    return findByPredicate(predicate);
  }
}
