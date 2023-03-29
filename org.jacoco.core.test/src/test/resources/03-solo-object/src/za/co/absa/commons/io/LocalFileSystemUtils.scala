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

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.{Files, Paths}

object LocalFileSystemUtils {

  /**
   * Check if a given files exists on the local file system
   */
  def localExists(path: String): Boolean = {
    new File(path).exists()
  }

  /**
   * Reads a local file fully and returns its content.
   *
   * @param path A path to a file.
   * @param charset A charset that is used in the file. By default UTF_8.
   * @return The file's content.
   */
  @deprecated("If possible, use more efficient org.apache.commons.io.FileUtils.readFileToString", "1.1.0")
  def readLocalFile(path: String, charset: Charset = StandardCharsets.UTF_8): String = {
    Files.readAllLines(Paths.get(path), charset).toArray.mkString("\n")
  }

  /**
   * Replaces tilde ('~') with the home dir.
   *
   * @param path An input path.
   * @return An absolute output path.
   */
  def replaceHome(path: String): String = {
    if (path.matches("^~.*")) {
      // not using replaceFirst as it interprets the backslash in Windows path as escape character mangling the result
      System.getProperty("user.home") + path.substring(1)
    } else {
      path
    }
  }

}
