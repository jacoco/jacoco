/*
 * Copyright 2021 ABSA Group Limited
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

package org.apache.commons.configuration

object SubsetConfigurationMethods {

  implicit class SubsetConfigurationOps(val conf: SubsetConfiguration) extends AnyVal {

    // The `SubsetConfiguration.getParentKey()` method is protected.
    // We have to use reflection call due to IllegalAccessError in some environments.
    // See: https://github.com/AbsaOSS/commons/issues/75
    def getParentKey: String => String = {
      val method = classOf[SubsetConfiguration].getDeclaredMethod("getParentKey", classOf[String])
      method.setAccessible(true)
      s => method.invoke(conf, s).asInstanceOf[String]
    }
  }

}
