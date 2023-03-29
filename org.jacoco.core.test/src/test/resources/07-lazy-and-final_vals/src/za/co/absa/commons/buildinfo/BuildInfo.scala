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

package za.co.absa.commons.buildinfo

import java.util.{MissingResourceException, Properties}

import za.co.absa.commons.lang.ARM.using
import za.co.absa.commons.lang.ImmutableProperties
import za.co.absa.commons.lang.OptionImplicits._

/**
 * A singleton that holds the project build version info taken from the build.properties file.
 *
 * It's particularly convenient for Maven-based projects. Just copy 'build.properties.template'
 * file to your classpath (without 'template' suffix) and enable Maven resource filtering.
 *
 * If BuildInfo defaults don't suit your project you can create your own instance
 * and parametrize it in one of the following ways:
 * {{{
 *   object MyBuildInfo extend BuildInfo(...)
 * }}}
 * or
 * {{{
 *   val myBuildInfo = BuildInfo(...)
 * }}}
 */
object BuildInfo extends BuildInfo(
  resourcePrefix = BuildInfoConst.DefaultResourcePrefix,
  propMapping = PropMapping.Default) {

  def apply(
    resourcePrefix: String = BuildInfoConst.DefaultResourcePrefix,
    propMapping: PropMapping = PropMapping.Default
  ): BuildInfo = new BuildInfo(resourcePrefix, propMapping) {}
}

object BuildInfoConst {
  // it has to be separated from the `BuildInfo` object due to a bug: https://github.com/scala/bug/issues/5000
  private[buildinfo] final val DefaultResourcePrefix = "/build"
}

abstract class BuildInfo(
  resourcePrefix: String = BuildInfoConst.DefaultResourcePrefix,
  propMapping: PropMapping = PropMapping.Default) {

  lazy val BuildProps: ImmutableProperties = {
    val resourceName = s"$resourcePrefix.properties"
    val stream =
      this.getClass.getResource(s"$resourceName")
        .asOption
        .map(_.openStream)
        .getOrElse(throw new MissingResourceException(resourceName, classOf[Properties].getName, resourceName))

    using(stream)(ImmutableProperties.fromStream)
  }

  lazy val Version: String = BuildProps.getProperty(propMapping.version)
  lazy val Timestamp: String = BuildProps.getProperty(propMapping.timestamp)
}
