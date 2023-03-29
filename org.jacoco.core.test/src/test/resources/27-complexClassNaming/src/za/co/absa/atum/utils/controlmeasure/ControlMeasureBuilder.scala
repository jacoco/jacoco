/*
 * Copyright 2018 ABSA Group Limited
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

package za.co.absa.atum.utils.controlmeasure

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.types.NumericType
import org.slf4j.LoggerFactory
import za.co.absa.atum.core.{Atum, ControlType, MeasurementProcessor}
import za.co.absa.atum.model.CheckpointImplicits.CheckpointExt
import za.co.absa.atum.model.{Checkpoint, ControlMeasure, ControlMeasureMetadata, Measurement}
import za.co.absa.atum.utils.controlmeasure.ControlMeasureBuilder.ControlTypeStrategy
import za.co.absa.atum.utils.controlmeasure.ControlMeasureBuilder.ControlTypeStrategy._
import za.co.absa.atum.utils.controlmeasure.ControlMeasureUtils.getTimestampAsString

import scala.util.Try

trait ControlMeasureBuilder {

  def withAggregateColumn(columnName: String, strategy: ControlTypeStrategy = ControlTypeStrategy.Default): ControlMeasureBuilder
  def withAggregateColumn(columnName: String, controlType: ControlType): ControlMeasureBuilder
  def withAggregateColumns(columnNames: Seq[String], strategy: ControlTypeStrategy = ControlTypeStrategy.Default): ControlMeasureBuilder
  def withAggregateColumns(controlTypeMappings: Seq[(String, ControlType)]): ControlMeasureBuilder

  def withSourceApplication(sourceApplication: String): ControlMeasureBuilder
  def withInputPath(inputPath: String): ControlMeasureBuilder
  def withReportDate(reportDate: String): ControlMeasureBuilder
  def withReportVersion(reportVersion: Int): ControlMeasureBuilder
  def withCountry(country: String): ControlMeasureBuilder
  def withHistoryType(historyType: String): ControlMeasureBuilder
  def withSourceType(sourceType: String): ControlMeasureBuilder
  def withInitialCheckpointName(initialCheckpointName: String): ControlMeasureBuilder
  def withWorkflowName(workflowName: String): ControlMeasureBuilder

  def build: ControlMeasure
}


object ControlMeasureBuilder {

  sealed trait ControlTypeStrategy
  object ControlTypeStrategy {

    private[controlmeasure] case class ControlTypeMapping(columnName: String, strategy: ControlTypeStrategy = Default)

    /**
     * For numeric types controlType.absAggregatedTotal and for non-numeric controlType.HashCrc32 is used.
     */
    case object Default extends ControlTypeStrategy

    /**
     * Specify the concrete control types to be used. If unusable
     * (e.g. AggregatedTotal or AbsAggregatedTotal for non-numeric, controlType is fallbacked by using the Default.
     *
     * @param controlType single controlType to be attempted to used for all aggregateColumns
     */
    case class Specific(controlType: ControlType) extends ControlTypeStrategy
  }

  /**
   * Get builder instance
   *
   * @param df               dataframe to build ControlMeasure from
   * @return ControlMeasureBuilder to continue with
   */
  def forDF(df: DataFrame): ControlMeasureBuilder =
    ControlMeasureBuilderImpl(df)

  /**
   * This class can be used to construct a [[ControlMeasure]] data object (to be used for content as source _INFO file)
   * for a given dataframe using the `build` "method" after all necessary fields have been set.
   *
   * The row count measurement is added automatically. You can also specify aggregation columns for aggregation measurements
   *
   * @param df                        A dataframe for which _INFO file to be created.
   * @param aggregateColumnMappings   Column names for `df` and their ControlTypeMappings (Default or Specific for each col)
   * @param sourceApplication         The name of the application providing the data (default = "").
   * @param inputPathName             The path to the input file name. Can be a folder with file mask (default = "").
   * @param reportDate                The date of the data generation (default = today).
   * @param reportVersion             The version of the data generation for the date, new versions replace old  versions of data (default = 1).
   * @param country                   Country name (default = "ZA").
   * @param historyType               History type (default = "Snapshot").
   * @param sourceType                Source type (default = "Source").
   * @param initialCheckpointName     The name of the initial checkpoint (default = "Source").
   * @param workflowName              A workflow name to group several checkpoint sth in the chain (default = "Source").
   *
   */
  private case class ControlMeasureBuilderImpl(df: DataFrame,
                                               aggregateColumnMappings: Seq[ControlTypeMapping] = Seq.empty,
                                               sourceApplication: String = "",
                                               inputPathName: String = "",
                                               reportDate: String = ControlMeasureUtils.getTodayAsString,
                                               reportVersion: Int = 1,
                                               country: String = "ZA",
                                               historyType: String = "Snapshot",
                                               sourceType: String = "Source",
                                               initialCheckpointName: String = "Source",
                                               workflowName: String = "Source"
                                              ) extends ControlMeasureBuilder {

    private val logger = LoggerFactory.getLogger(this.getClass)

    // these two are recommended values: failure to fill = warning
    def withSourceApplication(sourceApplication: String): ControlMeasureBuilderImpl = this.copy(sourceApplication = sourceApplication)
    def withInputPath(inputPath: String): ControlMeasureBuilderImpl = this.copy(inputPathName = inputPath)


    def withAggregateColumn(columnName: String, strategy: ControlTypeStrategy = Default): ControlMeasureBuilderImpl = {
      val mapping = ControlTypeMapping(columnName, strategy)
      this.withAggregateColumn(mapping)
    }

    def withAggregateColumn(columnName: String, controlType: ControlType): ControlMeasureBuilderImpl = {
      val mapping = ControlTypeMapping(columnName, Specific(controlType))
      this.withAggregateColumn(mapping)
    }

    private def withAggregateColumn(mapping: ControlTypeMapping): ControlMeasureBuilderImpl = {
      checkControlTypeSuitability(mapping)
      this.copy(aggregateColumnMappings = this.aggregateColumnMappings :+ mapping)
    }

    def withAggregateColumns(columnNames: Seq[String], strategy:ControlTypeStrategy = Default): ControlMeasureBuilderImpl = {
      val mappings = columnNames.map(ControlTypeMapping(_))
      this.withAggregateColumnsDirectly(mappings)
    }

    def withAggregateColumns(controlTypeMappings: Seq[(String, ControlType)]): ControlMeasureBuilderImpl = {
      val mappings = controlTypeMappings.map { case (name, controlType) =>
        ControlTypeMapping(name, Specific(controlType))
      }
      this.withAggregateColumnsDirectly(mappings)
    }

    private def withAggregateColumnsDirectly(mappings: Seq[ControlTypeMapping]): ControlMeasureBuilderImpl = {
      mappings.foreach(checkControlTypeSuitability)
      this.copy(aggregateColumnMappings = mappings)
    }

    def withReportDate(reportDate: String): ControlMeasureBuilderImpl = {
      if (Try(ControlMeasureUtils.dateFormat.parse(reportDate)).isFailure) {
        logger.error(s"Report date $reportDate does not validate against format ${ControlMeasureUtils.dateFormat}." +
          s"Consider checking correctness of the ControlMeasure")
      }
      this.copy(reportDate = reportDate)
    }
    def withReportVersion(reportVersion: Int): ControlMeasureBuilderImpl = this.copy(reportVersion = reportVersion)
    def withCountry(country: String): ControlMeasureBuilderImpl = this.copy(country = country)
    def withHistoryType(historyType: String): ControlMeasureBuilderImpl = this.copy(historyType = historyType)
    def withSourceType(sourceType: String): ControlMeasureBuilderImpl = this.copy(sourceType = sourceType)
    def withInitialCheckpointName(initialCheckpointName: String): ControlMeasureBuilderImpl = this.copy(initialCheckpointName = initialCheckpointName)
    def withWorkflowName(workflowName: String): ControlMeasureBuilderImpl = this.copy(workflowName = workflowName)

    /**
     * Constructs a [[ControlMeasure]] data object (to be used for content as source _INFO file)
     * for a given dataframe based on the all builder fields that have been set.
     *
     * The row count measurement is added automatically. You can also specify aggregation columns for aggregation measurements
     */
    lazy val build: ControlMeasure = {
      if (inputPathName.isEmpty) logger.warn("ControlMeasureBuilder's inputPathName is empty!")
      if (sourceApplication.isEmpty) logger.warn("ControlMeasureBuilder's sourceApplication is empty!")

      calculateMeasurement()
    }

    /**
     * Derives control type based on mapping and dataframe. Simply: if mapping contains a specific controlType, it is used.
     * If it contains Default, the controlType is derived based on column actual type (in df.schema)
     * @param mapping
     * @return controlType to be used
     */
    private[controlmeasure] def deriveControlType(mapping: ControlTypeMapping): ControlType = {
      mapping.strategy match {
        case Specific(controlType) => controlType // just use the specified controlType, checking is already done on builder
        case Default =>
          val dataType = df.select(mapping.columnName).schema.fields(0).dataType
          val isNumericDataType = dataType.isInstanceOf[NumericType]

          if (isNumericDataType) {
            ControlType.AbsAggregatedTotal
          } else {
            ControlType.HashCrc32
          }
      }
    }

    private def checkControlTypeSuitability(mapping: ControlTypeMapping): Unit = {
      require(df.columns.contains(mapping.columnName),
        s"Aggregate columns must be present in dataframe, unsatisfied for '${mapping.columnName}'." +
          s" Columns in the dataframe: ${df.columns.mkString(", ")}."
      )

      val dataType = df.select(mapping.columnName).schema.fields(0).dataType
      val isDataTypeNumeric = dataType.isInstanceOf[NumericType]

      mapping match {
        case ControlTypeMapping(columnName, Specific(controlType)) if controlType.onlyForNumeric && !isDataTypeNumeric =>
          Atum.log.warn(s"Column $columnName measurement $controlType requested, but the field is not numeric!"
            + s" Found: ${dataType.simpleString} datatype.")
        case _ =>
      }
    }

    def calculateMeasurement(): ControlMeasure = {
      // Calculate the measurements
      val timeStart = getTimestampAsString
      val rowCount = df.count()

      val aggregatedMeasurements = for (
        columnMapping <- aggregateColumnMappings
      ) yield {
        val columnName = columnMapping.columnName
        val controlType = deriveControlType(columnMapping)

        // measument processing shared from the measurement processor (that count measurements based on existing measurements)
        def measurementFunction(df: DataFrame): String = MeasurementProcessor.getMeasurementFunction(columnName, controlType)(df)

        Measurement(
          controlName = columnName + "ControlTotal",
          controlType = controlType.value,
          controlCol = columnName,
          controlValue = measurementFunction(df)
        )
      }
      val timeFinish = getTimestampAsString

      // Create a Control Measurement object
      ControlMeasure(metadata = ControlMeasureMetadata(
        sourceApplication = sourceApplication,
        country = country,
        historyType = historyType,
        dataFilename = inputPathName,
        sourceType = sourceType,
        version = reportVersion,
        informationDate = reportDate,
        additionalInfo = Map[String, String]()
      ), runUniqueId = None,
        Checkpoint(
          name = initialCheckpointName,
          processStartTime = timeStart,
          processEndTime = timeFinish,
          workflowName = workflowName,
          order = 1,
          controls = Measurement(
            controlName = "recordCount",
            controlType = ControlType.Count.value,
            controlCol = "*",
            controlValue = rowCount.toString
          ) :: aggregatedMeasurements.toList
        ).withBuildProperties :: Nil)
    }
  }
}
