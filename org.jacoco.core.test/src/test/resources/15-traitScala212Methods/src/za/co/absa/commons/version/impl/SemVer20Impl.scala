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

package za.co.absa.commons.version.impl

import za.co.absa.commons.version._
import za.co.absa.commons.version.impl.SemVer20Impl._

import scala.util.matching.Regex

/**
  * Semantic Versioning 2.0 implementation
  *
  * @see https://semver.org/spec/v2.0.0.html
  */
trait SemVer20Impl {

  def asSemVer(verStr: String): SemanticVersion = verStr match {
    case SemVerRegexp(major, minor, patch, preRelease, buildMeta) =>
      val mainComponents = Seq(
        NumericComponent(major.toInt),
        NumericComponent(minor.toInt),
        NumericComponent(patch.toInt))

      val optionalComponents = Seq(
        Option(preRelease).map(s => PreReleaseComponent(parseIdentifiers(s): _*)),
        Option(buildMeta).map(s => BuildMetadataComponent(parseIdentifiers(s): _*))
      ).flatten

      new Version(mainComponents ++ optionalComponents: _*) with SemVerOps

    case _ => throw new IllegalArgumentException(s"$verStr does not correspond to the SemVer 2.0 spec")
  }

  private def parseIdentifiers(str: String): Seq[Component] =
    str.split('.') map Component.apply
}

object SemVer20Impl {
  type SemanticVersion = Version with SemVerOps

  implicit val semVerOrdering: Ordering[SemanticVersion] = new Ordering[SemanticVersion] {
    override def compare(x: SemanticVersion, y: SemanticVersion): Int = x.compare(y)
  }

  /**
    * Implements SemVer 2.0 specific operations on the Version class
    */
  sealed trait SemVerOps {
    this: Version =>

    /**
      * A `core` part of the version number. E.g. "1.2.3" in "1.2.3-alpha.4+build.5"
      *
      * @return A new instance of [[SemanticVersion]] with the same `minor`, `major` and `patch`
      *         components, but without a `pre-release` or `build-meta` components
      */
    final def core: SemanticVersion =
      if (components.length == 3) this
      else new Version(this.components.take(3): _*) with SemVerOps

    /**
      * A `pre-release` part of the version number. E.g. "alpha.4" in "1.2.3-alpha.4+build.5"
      *
      * @return `Some(Version)` that consists of components from a `pre-release` part of this version,
      *         or `None` if a `pre-release` component is absent
      */
    final def preRelease: Option[Version] = components.collectFirst {
      case PreReleaseComponent(identifiers@_*) => Version(identifiers: _*)
    }

    /**
      * A `build metadata` part of the version number. E.g. "build.5" in "1.2.3-alpha.4+build.5"
      *
      * @return `Some(Version)` that consists of components from a `build-meta` part of this version,
      *         or `None` if a `build-meta` component is absent
      */
    final def buildMeta: Option[Version] = components.collectFirst {
      case BuildMetadataComponent(identifiers@_*) => Version(identifiers: _*)
    }

    /**
      * A `major` version number. E.g. "1" in "1.2.3-alpha.4+build.5"
      *
      * @return `major` component as a number
      */
    final def major: BigInt = numComponent(0)

    /**
      * A `minor` version number. E.g. "2" in "1.2.3-alpha.4+build.5"
      *
      * @return `minor` component as a number
      */
    final def minor: BigInt = numComponent(1)

    /**
      * A `patch` version number. E.g. "3" in "1.2.3-alpha.4+build.5"
      *
      * @return `patch` component as a number
      */
    final def patch: BigInt = numComponent(2)

    @inline private def numComponent(i: Int) = {
      val NumericComponent(x) = this.components(i)
      x
    }
  }

  private val SemVerRegexp: Regex = ("^" +
    "(0|[1-9]\\d*)\\." +
    "(0|[1-9]\\d*)\\." +
    "(0|[1-9]\\d*)" +
    "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
    "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?" +
    "$").r
}
