/*
 * Copyright 2020 ABSA Group Limited
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

package za.co.absa.commons.io

import java.net.URI
import java.nio.file.{Files, Path}

class TempFile private(prefix: String, suffix: String, pathOnly: Boolean) {
  val path: Path = Files.createTempFile(prefix, suffix)
  if (pathOnly) Files.delete(path)

  def deleteOnExit(): this.type = {
    path.toFile.deleteOnExit()
    this
  }

  def toURI: URI = path.toFile.toURI

  /**
   * The TempFile object will be converted to string representation which is stable and
   * equal on all platforms.
   *
   * @return string representation of current TempFile instance
   */
  def asString: String = path.toString.replace("\\", "/")
}

object TempFile {
  def apply(prefix: String = "", suffix: String = "", pathOnly: Boolean = false): TempFile =
    new TempFile(prefix, suffix, pathOnly)
}
