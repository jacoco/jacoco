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

package za.co.absa.enceladus

package object model {

  /**
    * The version of the data model.
    *
    * Each model version is expected to have a different schema.
    */
  final val ModelVersion = 1

  /**
    * The collection postfix to use for a non-zero model version.
    *
    * For instance, if the collection's base name is 'dataset', for model version 0 the collection name will
    * be 'dataset', for version 1 it will be 'dataset_v1', for version 2 it will be 'dataset_v2', etc.
    */
  final val CollectionSuffix = s"_v$ModelVersion"
}
