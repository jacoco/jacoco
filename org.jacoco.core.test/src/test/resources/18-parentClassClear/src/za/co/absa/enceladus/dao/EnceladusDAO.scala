/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.enceladus.dao

import org.apache.spark.sql.types.StructType
import za.co.absa.atum.model.{Checkpoint, ControlMeasure, RunStatus}
import za.co.absa.enceladus.dao.NotRetryableException.AuthenticationException
import za.co.absa.enceladus.model._
import za.co.absa.enceladus.utils.validation.ValidationLevel.Constants.DefaultValidationLevel
import za.co.absa.enceladus.utils.validation.ValidationLevel.ValidationLevel

/**
  * Trait for Enceladus DAO.
  */
trait EnceladusDAO {

  /**
    * Authenticates user with Enceladus
    */
  @throws[AuthenticationException]
  def authenticate(): Unit

  /**
    * Retrieves a specific dataset
    *
    * @param name    The dataset's name
    * @param version The dataset's version
    * @param validateProperties if set to other then  `NoValidation` datasets's `propertiesValidation` field will be populated
    * @return The retrieved dataset
    */
  def getDataset(name: String, version: Int, validateProperties: ValidationLevel = DefaultValidationLevel): Dataset

  /**
   * Retrieves properties to be written to the info file
   *
   * @param datasetName The dataset's name
   * @param datasetVersion The dataset's version
   * @return
   */
  def getDatasetPropertiesForInfoFile(datasetName: String, datasetVersion: Int): Map[String, String]

  /**
    * Retrieves a specific mapping table
    *
    * @param name    The mapping table's name
    * @param version The mapping table's version
    * @return The retrieved mapping table
    */
  def getMappingTable(name: String,
                      version: Int): MappingTable

  /**
    * Retrieves the spark representation of a specific schema
    *
    * @param name    The schema's name
    * @param version The schema's version
    * @return The spark representation of the retrieved schema
    */
  def getSchema(name: String,
                version: Int): StructType

  /**
    * Retrieves the file representation of a specific schema
    *
    * @param name    The schema's name
    * @param version The schema's version
    * @return The original representation of the retrieved schema (i.e., schema attachment)
    */
  def getSchemaAttachment(name: String, version: Int): String

  /**
    * Stores a new Run object in the database
    *
    * @param run A Run object
    * @return The Run as stored in database with a newly created unique ID and a run ID
    */
  def storeNewRunObject(run: Run): Run

  /**
    * Updates control measure object of the specified run
    *
    * @param uniqueId       An unique id of a Run object
    * @param controlMeasure Control Measures
    * @return true if Run object is successfully updated
    */
  def updateControlMeasure(uniqueId: String,
                           controlMeasure: ControlMeasure): Run

  /**
    * Updates status of the specified run
    *
    * @param uniqueId  An unique id of a run object
    * @param runStatus Status of a run object
    * @return The Run as stored in database with an updated run status
    */
  def updateRunStatus(uniqueId: String,
                      runStatus: RunStatus): Run

  /**
    * Updates spline reference of the specified run
    *
    * @param uniqueId  An unique id of a Run object
    * @param splineRef Spline Reference
    * @return true if Run object is successfully updated
    */
  def updateSplineReference(uniqueId: String,
                            splineRef: SplineReference): Run

  /**
    * Stores a new Run object in the database by loading control measurements from
    * _INFO file accompanied by output data
    *
    * @param uniqueId   An unique id of a Run object
    * @param checkpoint A checkpoint to be appended to the database
    * @return true if Run object is successfully updated
    */
  def appendCheckpointMeasure(uniqueId: String,
                              checkpoint: Checkpoint): Run

}
