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

package za.co.absa.enceladus.utils.schema

import org.apache.spark.sql.types._
import za.co.absa.spark.commons.implicits.StructFieldImplicits.StructFieldMetadataEnhancements
import za.co.absa.spark.commons.utils.SchemaUtils.appendPath

import scala.annotation.tailrec

object SchemaUtils {

  /**
    * Returns all renames in the provided schema.
    * @param schema                       schema to examine
    * @param includeIfPredecessorChanged  if set to true, fields are included even if their name have not changed but
    *                                     a predecessor's (parent, grandparent etc.) has
    * @return        the keys of the returned map are the columns' names after renames, the values are the source columns;
    *                the name are full paths denoted with dot notation
    */
  def getRenamesInSchema(schema: StructType, includeIfPredecessorChanged: Boolean = true): Map[String, String] = {

    def getRenamesRecursively(path: String,
                              sourcePath: String,
                              struct: StructType,
                              renamesAcc: Map[String, String],
                              predecessorChanged: Boolean): Map[String, String] = {

      struct.fields.foldLeft(renamesAcc) { (renamesSoFar, field) =>
        val fieldFullName = appendPath(path, field.name)
        val fieldSourceName = field.metadata.getOptString(MetadataKeys.SourceColumn).getOrElse(field.name)
        val fieldFullSourceName = appendPath(sourcePath, fieldSourceName)

        val (renames, renameOnPath) = if ((fieldSourceName != field.name) || (predecessorChanged && includeIfPredecessorChanged)) {
          (renamesSoFar + (fieldFullName -> fieldFullSourceName), true)
        } else {
          (renamesSoFar, predecessorChanged)
        }

        field.dataType match {
          case st: StructType => getRenamesRecursively(fieldFullName, fieldFullSourceName, st, renames, renameOnPath)
          case at: ArrayType  => getStructInArray(at.elementType).fold(renames) { item =>
              getRenamesRecursively(fieldFullName, fieldFullSourceName, item, renames, renameOnPath)
            }
          case _              => renames
        }
      }
    }

    @tailrec
    def getStructInArray(dataType: DataType): Option[StructType] = {
      dataType match {
        case st: StructType => Option(st)
        case at: ArrayType => getStructInArray(at.elementType)
        case _ => None
      }
    }

    getRenamesRecursively("", "", schema, Map.empty, predecessorChanged = false)
  }

}
