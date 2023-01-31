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
package ai.startree.thirdeye.plugins.postprocessor;

import static ai.startree.thirdeye.plugins.postprocessor.AnomalyMergerPostProcessor.DEFAULT_MERGE_MAX_GAP;
import static ai.startree.thirdeye.plugins.postprocessor.AnomalyMergerPostProcessor.newAfterReplayLabel;
import static ai.startree.thirdeye.plugins.postprocessor.AnomalyMergerPostProcessor.newOutdatedLabel;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.DetectionPipelineUsage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.chrono.ISOChronology;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class AnomalyMergerPostProcessorTest {

  public static final long ALERT_ID = 1L;
  private static final long JANUARY_1_2021_01H = 1609462800_000L;
  private static final long JANUARY_1_2021_02H = 1609466400_000L;
  private static final long JANUARY_1_2021_03H = 1609470000_000L;
  private static final long JANUARY_1_2021_04H = 1609473600_000L;
  private static final long JANUARY_1_2021_05H = 1609477200_000L;
  private static final long JANUARY_1_2021_06H = 1609480800_000L;
  private static long ANOMALY_ID = 1000L;
  private AnomalyManager anomalyManager;
  private AnomalyMergerPostProcessorSpec detectionSpec;
  private AnomalyMergerPostProcessor detectionMerger;

  private static EnumerationItemDTO newEnumerationItemRef(final long enumerationItemId) {
    final EnumerationItemDTO enumerationItemDTO = new EnumerationItemDTO();
    enumerationItemDTO.setId(enumerationItemId);
    return enumerationItemDTO;
  }

  private static AnomalyDTO newAnomaly(final long startDate, final long endDate) {
    final AnomalyDTO anomaly = new AnomalyDTO();
    anomaly.setStartTime(startDate);
    anomaly.setEndTime(endDate);
    return anomaly;
  }

  private static AnomalyDTO existingAnomaly(final long startDate, final long endDate) {
    final AnomalyDTO anomaly = newAnomaly(startDate, endDate);
    anomaly.setId(++ANOMALY_ID);
    return anomaly;
  }

  private static long plusMin(final long startDate, final int minutes) {
    return new DateTime(startDate, DateTimeZone.UTC).plus(Period.minutes(minutes)).getMillis();
  }

  public static boolean isSameAnomaly(AnomalyDTO a1, AnomalyDTO a2) {
    return Objects.equals(a1.getId(), a2.getId()) && a1.getStartTime() == a2.getStartTime()
        && a1.getEndTime() == a2.getEndTime();
  }

  @BeforeMethod
  public void setUp() {
    anomalyManager = mock(AnomalyManager.class);
    detectionSpec = new AnomalyMergerPostProcessorSpec().setAnomalyManager(
        anomalyManager).setAlertId(ALERT_ID).setUsage(DetectionPipelineUsage.DETECTION);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    detectionMerger.setChronology(ISOChronology.getInstanceUTC());
  }

  @Test
  public void testPrepareSortedAnomalyList() {
    assertThat(AnomalyMergerPostProcessor.combineAndSort(emptyList(), emptyList())).isEqualTo(
        emptyList());

    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO new2 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO existing2 = existingAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);

    assertThat(
        AnomalyMergerPostProcessor.combineAndSort(emptyList(), singletonList(existing1))).isEqualTo(
        singletonList(existing1));

    assertThat(
        AnomalyMergerPostProcessor.combineAndSort(singletonList(new1), emptyList())).isEqualTo(
        singletonList(new1));

    assertThat(AnomalyMergerPostProcessor.combineAndSort(singletonList(new1),
        singletonList(existing1))).isEqualTo(List.of(existing1, new1));

    assertThat(AnomalyMergerPostProcessor.combineAndSort(List.of(new1, new2),
        List.of(existing1, existing2))).isEqualTo(List.of(existing1, new1, existing2, new2));
  }

  @Test
  public void testEmptyMergeAndSave() {
    assertThat(detectionMerger.merge(emptyList())).isEqualTo(List.of());
  }

  @Test
  public void testSingleAnomalyNoMerge() {
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> emptyList());
    final AnomalyDTO newAnomaly = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final List<AnomalyDTO> output = detectionMerger.merge(singletonList(newAnomaly));
    assertThat(output).isEqualTo(List.of(newAnomaly));
  }

  @Test
  public void testSingleAnomalyMergeWithExisting() {
    final AnomalyDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> singletonList(existingAnomaly));

    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(new1));
    assertThat(output).isEqualTo(List.of(existingAnomaly));
    assertThat(existingAnomaly.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testSingleAnomalyNoMergeIfPatternIsUpVsDown() {
    final AnomalyDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    // existing anomaly is pattern UP
    existingAnomaly.setAvgBaselineVal(0);
    existingAnomaly.setAvgCurrentVal(10);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> singletonList(existingAnomaly));

    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    // existing anomaly is pattern DOWN
    new1.setAvgBaselineVal(10);
    new1.setAvgCurrentVal(0);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(new1));
    assertThat(output).isEqualTo(List.of(existingAnomaly, new1));
    assertThat(existingAnomaly.getChildren().isEmpty()).isTrue();
    assertThat(new1.getChildren().isEmpty()).isTrue();
  }

  @Test
  public void testSingleAnomalyNoMergeWithExistingIfUsageIsEvaluate() {
    final AnomalyDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> singletonList(existingAnomaly));

    detectionSpec.setUsage(DetectionPipelineUsage.EVALUATION);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(new1));
    assertThat(output).isEqualTo(List.of(new1));
    assertThat(new1.getChildren().size()).isEqualTo(0);
  }

  @Test
  public void testMergeAllNewAndExistingAnomalies() {
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H);
    final AnomalyDTO new2 = newAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H);
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO existing2 = existingAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1, new2), List.of(existing1, existing2));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);
    assertThat(merged.size()).isEqualTo(1);

    assertThat(isSameAnomaly(merged.get(0), existing1)).isTrue();
  }

  @Test
  public void testMergeNoMergeWhenAnomaliesSpacedByMoreThanMergeGap() {
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    // new anomaly that happens after the merge gap
    final long afterMergeGapStart = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(
        DEFAULT_MERGE_MAX_GAP).plus(1).getMillis();
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final AnomalyDTO new2 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1, new2), List.of());
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
    assertThat(merged.get(0)).isEqualTo(new1);
    assertThat(merged.get(1)).isEqualTo(new2);
  }

  @Test
  public void testMergeNoMergeWhenAnomaliesByMoreThanMergeGapWithExisting() {
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    // new anomaly that happens before the merge gap
    final long afterMergeGapStart = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(
        DEFAULT_MERGE_MAX_GAP).plus(1).getMillis();
    final long afterMergeGapEnd = plusMin(afterMergeGapStart, 60);
    final AnomalyDTO new1 = newAnomaly(afterMergeGapStart, afterMergeGapEnd);
    final long afterMergeGapStart2 = new DateTime(afterMergeGapEnd, DateTimeZone.UTC).plus(
        DEFAULT_MERGE_MAX_GAP).plus(1).getMillis();
    final long afterMergeGapEnd2 = plusMin(afterMergeGapStart2, 60);
    final AnomalyDTO new2 = newAnomaly(afterMergeGapStart2, afterMergeGapEnd2);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1, new2), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(3);
    assertThat(merged.get(0)).isEqualTo(existing1);
    assertThat(merged.get(1)).isEqualTo(new1);
    assertThat(merged.get(2)).isEqualTo(new2);
  }

  @Test
  public void testMergeNewInExisting() {
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final long expectedId = existing1.getId();
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO new1 = newAnomaly(
        new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(1)).getMillis(),
        newEndTime);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    AnomalyDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(newEndTime);
    assertThat(parent.getId()).isEqualTo(expectedId);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeNoMergeWithZeroMergeGapPeriod() {
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    detectionSpec.setMergeMaxGap(Period.ZERO.toString());
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    detectionMerger.setChronology(ISOChronology.getInstanceUTC());
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
  }

  @Test
  public void testMergeNoMergeWithLabelsWithIgnoreDefaultDifferent() {
    // never merge anomalies with different ignore default
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(List.of(new AnomalyLabelDTO().setIgnore(true)));
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(false)));
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
  }

  @Test
  public void testMergeDoMergeWithLabelsWithIgnoreDefaultSameFalse() {
    // merge anomalies with ignore default both false
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H)
        // ignore=false if there is no label
        .setAnomalyLabels(List.of());
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(false)));
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
  }

  @Test
  public void testMergeDoMergeWithLabelsWithIgnoreDefaultSameTrue() {
    // merge anomalies with ignore default both true
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(List.of(new AnomalyLabelDTO().setIgnore(true)));
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(true)));
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
  }

  @Test
  public void testMergeLabelsAreMergedCorrectly() {
    // check that labels that are equal are not duplicated in the merger
    final AnomalyLabelDTO christmasEveLabel = new AnomalyLabelDTO().setName("Holiday")
        .setMetadata(Map.of("eventName", "Christmas Eve"))
        .setIgnore(false);
    final AnomalyLabelDTO coldStartLabel = new AnomalyLabelDTO().setName("ColdStart")
        .setIgnore(true);
    final List<AnomalyLabelDTO> existingLabels = List.of(christmasEveLabel, coldStartLabel);
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(existingLabels);
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyLabelDTO christmasDayLabel = new AnomalyLabelDTO().setName("Holiday")
        .setMetadata(Map.of("eventName", "Christmas Day"))
        .setIgnore(false);
    final List<AnomalyLabelDTO> newLabels = List.of(christmasDayLabel, coldStartLabel);
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        newLabels);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    assertThat(merged.get(0).getAnomalyLabels().size()).isEqualTo(3);
  }

  @Test
  public void testMergeDoMergeWithLabelsWithExistingIgnoreDefaultSameTrue() {
    // never merge anomalies with different ignore default
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setAnomalyLabels(List.of(new AnomalyLabelDTO().setIgnore(true)));
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_02H, newEndTime).setAnomalyLabels(
        List.of(new AnomalyLabelDTO().setIgnore(true)));
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
  }

  @Test
  public void testMergeExistingInNew() {
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final long newEndTime = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(2))
        .getMillis();
    final AnomalyDTO existing1 = existingAnomaly(
        new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.hours(1)).getMillis(),
        newEndTime);
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    AnomalyDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(newEndTime);
    assertThat(parent.getId()).isEqualTo(null);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeNewIncludedInExisting() {
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H);
    final long expectedId = existing1.getId();
    final AnomalyDTO new1 = newAnomaly(
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(10)).getMillis(),
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(30)).getMillis());
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));

    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    AnomalyDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(JANUARY_1_2021_02H);
    assertThat(parent.getId()).isEqualTo(expectedId);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeExistingIncludedInNew() {
    final AnomalyDTO new1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO existing1 = existingAnomaly(
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(10)).getMillis(),
        new DateTime(JANUARY_1_2021_01H, DateTimeZone.UTC).plus(Period.minutes(30)).getMillis());
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(1);
    AnomalyDTO parent = merged.get(0);
    assertThat(parent.getStartTime()).isEqualTo(JANUARY_1_2021_01H);
    assertThat(parent.getEndTime()).isEqualTo(JANUARY_1_2021_02H);
    assertThat(parent.getId()).isEqualTo(null);
    assertThat(parent.getChildren().size()).isEqualTo(2);
  }

  @Test
  public void testMergeNoMergeIfAnomalyWouldBecomeBiggerThanMaxDuration() {
    final long endExisting = new DateTime(JANUARY_1_2021_02H, DateTimeZone.UTC).plus(Period.days(4))
        .getMillis();
    // 4 days anomaly
    final AnomalyDTO existing1 = existingAnomaly(JANUARY_1_2021_01H, endExisting);
    // 4 days anomaly
    final AnomalyDTO new1 = newAnomaly(endExisting,
        new DateTime(endExisting, DateTimeZone.UTC).plus(Period.days(4)).getMillis());
    final List<AnomalyDTO> sorted = AnomalyMergerPostProcessor.combineAndSort(
        List.of(new1), List.of(existing1));
    final List<AnomalyDTO> merged = detectionMerger.doMerge(sorted);

    assertThat(merged.size()).isEqualTo(2);
    assertThat(merged.get(0)).isEqualTo(existing1);
    assertThat(merged.get(1)).isEqualTo(new1);
  }

  // note: there is no need to test for multiple enumerationItems - merger is run for each enumeration independently
  @Test
  public void testSingleAnomalyWithMergeAndSaveWithEnumerationItem() {
    final long enumerationItemId = 1L;
    final EnumerationItemDTO ei1 = newEnumerationItemRef(enumerationItemId);
    final AnomalyDTO existingAnomaly = existingAnomaly(JANUARY_1_2021_01H,
        JANUARY_1_2021_02H).setEnumerationItem(ei1);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), eq(enumerationItemId))).thenAnswer(
        i -> singletonList(existingAnomaly));
    final EnumerationItemDTO enumerationDTO = (EnumerationItemDTO) new EnumerationItemDTO().setId(
        enumerationItemId);
    detectionSpec.setEnumerationItemDTO(enumerationDTO);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final List<AnomalyDTO> output = detectionMerger.merge(
        List.of(newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H).setEnumerationItem(ei1),
            newAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H).setEnumerationItem(ei1)));

    assertThat(output).isEqualTo(List.of(existingAnomaly));
  }

  // tests below check the replay merging rules - see https://docs.google.com/document/d/1bSbv4XhTQsdGR1XVM_dYL1cK9Q6JlntvzNYmmMiXQRI/edit#
  @Test
  public void testReplayRule2PipelineAnomalyIsNewInTheMiddleNotMerged() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_05H, JANUARY_1_2021_06H);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, n1, e2));
  }

  @Test
  public void testReplayRule2PipelineAnomalyIsNewLeftMostNotMerged() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_05H, JANUARY_1_2021_06H);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(n1, e1, e2));
  }

  @Test
  public void testReplayRule2PipelineAnomalyIsNewRightMostNotMerged() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_05H, JANUARY_1_2021_06H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, e2, n1));
  }

  @Test
  public void testReplayRule2PipelineAnomalyIsNewRightMostAndMerged() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, e2));
    assertThat(e2.getChildren().size()).isEqualTo(2);
    // the other anomaly is a copy of e2 - not tested here
    assertThat(e2.getChildren().contains(n1)).isTrue();
  }

  @Test
  public void testReplayRule2PipelineAnomalyIsNewAndBecomesAParent() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, n1));
    assertThat(n1.getChildren().size()).isEqualTo(2);
    // the other anomaly is a copy of n1 - not tested here
    assertThat(n1.getChildren().contains(e2)).isTrue();
  }

  @Test
  public void testReplayRule3PipelineAnomalyMatchesExistingChild() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H)
        .setAvgCurrentVal(10)
        .setAvgBaselineVal(12)
        .setScore(1)
        .setChild(true);
    e1.setChildren(Set.of(e2));
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H)
        .setAvgCurrentVal(23)
        .setAvgBaselineVal(37)
        .setScore(0.5);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e2, e1));
    assertThat(e1.getChildren().iterator().next()).isEqualTo(e2);
    assertThat(e2.isChild()).isTrue();
    assertThat(e2.getAnomalyLabels()).isNull();
    assertThat(e2.getAvgCurrentVal()).isEqualTo(23);
    assertThat(e2.getAvgBaselineVal()).isEqualTo(37);
    assertThat(e2.getScore()).isEqualTo(0.5);
  }

  @Test
  public void testReplayRule3PipelineAnomalyMatchesExistingAnomalyWithNoChild() {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H)
        .setAvgCurrentVal(10)
        .setAvgBaselineVal(12)
        .setScore(1);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H)
        .setAvgCurrentVal(23)
        .setAvgBaselineVal(37)
        .setScore(0.5);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, e2));
    assertThat(e2.getAnomalyLabels()).isNull();
    assertThat(e2.getAvgCurrentVal()).isEqualTo(23);
    assertThat(e2.getAvgBaselineVal()).isEqualTo(37);
    assertThat(e2.getScore()).isEqualTo(0.5);
  }

  // existing labels not overriden + not duplicated?
  // - what if there are already outdated where outdated exist --> skip them?

  @DataProvider(name = "rule3bisSameAsRule3Cases")
  public Object[][] rule3bisSameAsRule3Cases() {
    // one alert for each template
    final Object[] percentageChangeBelowThreshold = {null, 300.};
    final Object[] absoluteChangeBelowThreshold = {30., null};
    final Object[] absoluteAndPercentageChangeBelowThreshold = {30., 300.};
    final Object[] absoluteAbovePercentageBelow = {1., 300.};
    final Object[] absoluteBelowPercentageAbove = {30., 10.};
    return new Object[][]{percentageChangeBelowThreshold, absoluteChangeBelowThreshold,
        absoluteAndPercentageChangeBelowThreshold, absoluteAbovePercentageBelow,
        absoluteBelowPercentageAbove};
  }

  @Test(dataProvider = "rule3bisSameAsRule3Cases")
  public void testReplayRule3BisBehaveLikeRule3(final Double renotifyAbsoluteThreshold,
      final Double renotifyPercentageThreshold) {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H)
        .setAvgCurrentVal(10)
        .setAvgBaselineVal(12)
        .setScore(1);
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionSpec.setReNotifyAbsoluteThreshold(renotifyAbsoluteThreshold);
    detectionSpec.setReNotifyPercentageThreshold(renotifyPercentageThreshold);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H)
        .setAvgCurrentVal(23)
        .setAvgBaselineVal(37)
        .setScore(0.5);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, e2));
    assertThat(e2.getAnomalyLabels()).isNull();
    assertThat(e2.getAvgCurrentVal()).isEqualTo(23);
    assertThat(e2.getAvgBaselineVal()).isEqualTo(37);
    assertThat(e2.getScore()).isEqualTo(0.5);
  }

  @Test(dataProvider = "rule3bisSameAsRule3Cases")
  public void testReplayRule3BisBehaveLikeRule3WithChildInsideParentUpdated(final Double renotifyAbsoluteThreshold,
      final Double renotifyPercentageThreshold) {
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H);
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H)
        .setAvgCurrentVal(10)
        .setAvgBaselineVal(12)
        .setScore(1)
        .setChild(true);
    e1.setChildren(new HashSet<>(Set.of(e2)));
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionSpec.setReNotifyAbsoluteThreshold(renotifyAbsoluteThreshold);
    detectionSpec.setReNotifyPercentageThreshold(renotifyPercentageThreshold);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_02H, JANUARY_1_2021_03H)
        .setAvgCurrentVal(23)
        .setAvgBaselineVal(37)
        .setScore(0.5);
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e2, e1));
    assertThat(e1.getChildren().iterator().next()).isEqualTo(e2);
    assertThat(e2.isChild()).isTrue();
    assertThat(e2.getAnomalyLabels()).isNull();
    assertThat(e2.getAvgCurrentVal()).isEqualTo(23);
    assertThat(e2.getAvgBaselineVal()).isEqualTo(37);
    assertThat(e2.getScore()).isEqualTo(0.5);
  }

  @DataProvider(name = "rule3bisWithNewAnomalyCreatedAndOldOutdated")
  public Object[][] rule3bisWithNewAnomalyCreatedAndOldOutdated() {
    // one alert for each template
    final Object[] percentageChangeAboveThreshold = {0., 10.};
    final Object[] absoluteChangeAboveThreshold = {5., 0.};
    final Object[] absoluteAndPercentageChangeAboveThreshold = {5., 10.};
    return new Object[][]{percentageChangeAboveThreshold, absoluteChangeAboveThreshold,
        absoluteAndPercentageChangeAboveThreshold};
  }

  @Test(dataProvider = "rule3bisWithNewAnomalyCreatedAndOldOutdated")
  public void testReplayRule3BisWithNewAnomalyCreatedAndOldOutdated(
      final Double renotifyAbsoluteThreshold,
      final Double renotifyPercentageThreshold) {
    final AnomalyLabelDTO label = new AnomalyLabelDTO().setName("TEST_LABEL");
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H).setAnomalyLabels(
        new ArrayList<>(List.of(label)));
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H)
        .setAvgCurrentVal(10)
        .setAvgBaselineVal(12)
        .setScore(1)
        .setAnomalyLabels(new ArrayList<>(List.of(label)));
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionSpec.setReNotifyAbsoluteThreshold(renotifyAbsoluteThreshold);
    detectionSpec.setReNotifyPercentageThreshold(renotifyPercentageThreshold);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_04H, JANUARY_1_2021_05H)
        .setAvgCurrentVal(23)
        .setAvgBaselineVal(37)
        .setScore(0.5)
        .setAnomalyLabels(new ArrayList<>(List.of(label)));
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e1, e2, n1));
    assertThat(e1.getAnomalyLabels()).isEqualTo(List.of(label));
    assertThat(e1.getAnomalyLabels().size()).isEqualTo(1);
    assertThat(e1.getAnomalyLabels()).isEqualTo(List.of(label));

    assertThat(e2.getAnomalyLabels().size()).isEqualTo(2);
    assertThat(e2.getAnomalyLabels()).isEqualTo(List.of(label, newOutdatedLabel()));
    assertThat(e2.getAvgCurrentVal()).isEqualTo(10);
    assertThat(e2.getAvgBaselineVal()).isEqualTo(12);
    assertThat(e2.getScore()).isEqualTo(1);

    assertThat(n1.getAnomalyLabels().size()).isEqualTo(2);
    assertThat(n1.getAnomalyLabels()).isEqualTo(List.of(label, newAfterReplayLabel()));
    assertThat(n1.getAvgCurrentVal()).isEqualTo(23);
    assertThat(n1.getAvgBaselineVal()).isEqualTo(37);
    assertThat(n1.getScore()).isEqualTo(0.5);
  }

  @Test(dataProvider = "rule3bisWithNewAnomalyCreatedAndOldOutdated")
  public void testReplayRule3BisWithNewAnomalyCreatedAndOldOutdatedWithChildInsideParentUpdated(
      final Double renotifyAbsoluteThreshold,
      final Double renotifyPercentageThreshold) {
    final AnomalyLabelDTO label = new AnomalyLabelDTO().setName("TEST_LABEL");
    final AnomalyDTO e1 = existingAnomaly(JANUARY_1_2021_01H, JANUARY_1_2021_02H).setAnomalyLabels(
        new ArrayList<>(List.of(label)));
    final AnomalyDTO e2 = existingAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H)
        .setAvgCurrentVal(10)
        .setAvgBaselineVal(12)
        .setScore(1)
        .setAnomalyLabels(new ArrayList<>(List.of(label)))
        .setChild(true);
    e1.setChildren(new HashSet<>(Set.of(e2)));
    final List<AnomalyDTO> existingAnomalies = List.of(e1, e2);
    when(anomalyManager.findByStartEndTimeInRangeAndDetectionConfigId(anyLong(),
        anyLong(), anyLong(), isNull())).thenAnswer(i -> existingAnomalies);
    detectionSpec.setMergeMaxGap("PT30M");
    detectionSpec.setReNotifyAbsoluteThreshold(renotifyAbsoluteThreshold);
    detectionSpec.setReNotifyPercentageThreshold(renotifyPercentageThreshold);
    detectionMerger = new AnomalyMergerPostProcessor(detectionSpec);
    final AnomalyDTO n1 = newAnomaly(JANUARY_1_2021_03H, JANUARY_1_2021_04H)
        .setAvgCurrentVal(23)
        .setAvgBaselineVal(37)
        .setScore(0.5)
        .setAnomalyLabels(new ArrayList<>(List.of(label)));
    final List<AnomalyDTO> output = detectionMerger.merge(List.of(n1));

    assertThat(output).isEqualTo(List.of(e2, e1, n1));
    assertThat(e1.getAnomalyLabels()).isEqualTo(List.of(label));
    assertThat(e1.getAnomalyLabels().size()).isEqualTo(1);
    assertThat(e1.getAnomalyLabels()).isEqualTo(List.of(label));
    assertThat(e1.getChildren().size()).isEqualTo(1);
    assertThat(e1.getChildren().iterator().next()).isEqualTo(e2);

    assertThat(e2.getAnomalyLabels().size()).isEqualTo(2);
    assertThat(e2.getAnomalyLabels()).isEqualTo(List.of(label, newOutdatedLabel()));
    assertThat(e2.isChild()).isTrue();
    assertThat(e2.getAvgCurrentVal()).isEqualTo(10);
    assertThat(e2.getAvgBaselineVal()).isEqualTo(12);
    assertThat(e2.getScore()).isEqualTo(1);

    assertThat(n1.getAnomalyLabels().size()).isEqualTo(2);
    assertThat(n1.getAnomalyLabels()).isEqualTo(List.of(label, newAfterReplayLabel()));
    assertThat(n1.isChild()).isFalse();
    assertThat(n1.getAvgCurrentVal()).isEqualTo(23);
    assertThat(n1.getAvgBaselineVal()).isEqualTo(37);
    assertThat(n1.getScore()).isEqualTo(0.5);
  }

  // not implemented: what if a parent now contains an anomaly to ignore - recompute the merge? recompute the boundaries?
  // in this first implem nothing is done - we consider as long as there is one anomaly, this is still an anomaly, and it was notified anyway and the merge is not a big issue
  // later: all parents merge edge cases should be implemented

  // rule 4 is not implemented
}

