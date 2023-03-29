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

package za.co.absa.enceladus.rest_api.services

import org.mongodb.scala.result.UpdateResult
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import za.co.absa.enceladus.model.{ModelVersion, Schema, UsedIn, Validation}
import za.co.absa.enceladus.model.versionedModel.{VersionedModel, VersionedSummary}
import za.co.absa.enceladus.rest_api.exceptions._
import za.co.absa.enceladus.rest_api.repositories.VersionedMongoRepository
import za.co.absa.enceladus.model.backend.audit._

import scala.concurrent.Future
import com.mongodb.MongoWriteException
import VersionedModelService._
import za.co.absa.enceladus.rest_api.exceptions.LockedEntityException

// scalastyle:off number.of.methods
trait VersionedModelService[C <: VersionedModel with Product with Auditable[C]]
  extends ModelService[C] {

  def mongoRepository: VersionedMongoRepository[C]

  import scala.concurrent.ExecutionContext.Implicits.global

  private[services] val logger = LoggerFactory.getLogger(this.getClass)

  def getLatestVersionsSummarySearch(searchQuery: Option[String],
                                     offset: Option[Int],
                                     limit: Option[Int]): Future[Seq[VersionedSummary]] = {
    mongoRepository.getLatestVersionsSummarySearch(searchQuery, offset, limit)
  }

  def getLatestVersions(): Future[Seq[C]] = {
    mongoRepository.getLatestVersions(None)
  }

  def getSearchSuggestions(): Future[Seq[String]] = {
    mongoRepository.getDistinctNamesEnabled()
  }

  def getVersion(name: String, version: Int): Future[Option[C]] = {
    mongoRepository.getVersion(name, version)
  }

  def getAllVersions(name: String): Future[Seq[C]] = {
    mongoRepository.getAllVersions(name)
  }

  def getLatestVersion(name: String): Future[Option[C]] = {
    mongoRepository.getLatestVersionValue(name).flatMap({
      case Some(version) => getVersion(name, version)
      case _ => throw NotFoundException()
    })
  }

  def getLatestVersionNumber(name: String): Future[Int] = {
    mongoRepository.getLatestVersionValue(name).flatMap({
      case Some(version) => Future(version)
      case _ => throw NotFoundException()
    })
  }

  def getLatestVersionValue(name: String): Future[Option[Int]] = {
    mongoRepository.getLatestVersionValue(name)
  }

  def exportSingleItem(name: String, version: Int): Future[String] = {
    getVersion(name, version).flatMap({
      case Some(item) => Future(item.exportItem())
      case _ => throw NotFoundException()
    })
  }

  def exportLatestItem(name: String): Future[String] = {
    getLatestVersion(name).flatMap({
      case Some(item) => Future(item.exportItem())
      case _ => throw NotFoundException()
    })
  }

  // v2 has external validate validation applied only to imports (not create/edits) via validateSingleImport
  def importSingleItem(item: C, username: String, metadata: Map[String, String]): Future[Option[(C, Validation)]] = {
    for {
      validation <- validateSingleImport(item, metadata)
      result <- {
        if (validation.isValid) {
          importItem(item, username)
        } else {
          throw ValidationException(validation)
        }
      }
    } yield result
  }

  private[services] def validateSingleImport(item: C, metadata: Map[String, String]): Future[Validation] = {
    val validation = Validation()
      .withErrorIf(!hasValidNameChars(item.name), "item.name", s"name '${item.name}' contains unsupported characters")
      .withErrorIf(item.parent.isDefined, "item.parent", "parent should not be defined on import")
    val withMetadataValidation = validation.merge(validateMetadata(metadata))
    Future(withMetadataValidation)
  }

  private[services] def validateMetadata(metadata: Map[String, String]): Validation = {
    def exportVersionErrorMessage(version: String) = {
      s"""Export/Import API version mismatch. Acceptable version is $ModelVersion. Version passed is $version"""
    }

    Validation()
      .withErrorIf(
        !hasValidApiVersion(metadata.get("exportVersion")),
        "metadata.exportApiVersion",
        exportVersionErrorMessage(metadata.getOrElse("exportVersion", "null"))
      )
  }

  private[services] def importItem(item: C, username: String): Future[Option[(C, Validation)]]

  private[services] def validateSchema(schemaName: String,
                                       schemaVersion: Int,
                                       maybeSchema: Future[Option[Schema]]): Future[Validation] = {
    for {
      schema <- maybeSchema
    } yield Validation().withErrorIf(
      schema.isEmpty,
      "item.schema",
      s"schema $schemaName v$schemaVersion defined for the dataset could not be found"
    )
  }

  private[services] def getParents(name: String, fromVersion: Option[Int] = None): Future[Seq[C]] = {
    for {
      versions <- {
        //store all in version ascending order
        val all = mongoRepository.getAllVersions(name, inclDisabled = true).map(_.sortBy(_.version))
        //get those relevant to us
        if (fromVersion.isDefined) {
          all.map(_.filter(_.version <= fromVersion.get))
        } else {
          all
        }
      }
      res <- {
        //see if this was branched from a different entity
        val topParent = if (versions.isEmpty || versions.head.parent.isEmpty) {
          None
        } else {
          versions.head.parent
        }
        if (topParent.isDefined) {
          getParents(topParent.get.name, Some(topParent.get.version))
        } else {
          Future.successful(Seq())
        }
      }
    } yield res ++ versions
  }

  def getAuditTrail(name: String): Future[AuditTrail] = {
    val allParents = getParents(name)

    allParents.flatMap({ parents =>
      val msgs = if (parents.size < 2) Seq() else {
        val pairs = parents.sliding(2)
        pairs.map(p => p.head.getAuditMessages(p(1))).toSeq
      }
      if (parents.isEmpty) {
        this.getLatestVersion(name).map({
          case Some(entity) => AuditTrail(msgs.reverse :+ entity.createdMessage)
          case None => throw NotFoundException()
        })
      } else {
        Future(AuditTrail(msgs.reverse :+ parents.head.createdMessage))
      }
    })
  }

  def getUsedIn(name: String, version: Option[Int]): Future[UsedIn]

  private[rest_api] def create(item: C, username: String): Future[Option[(C, Validation)]] = {
    // individual validations are deliberately not run in parallel - the latter may not be needed if the former fails
    for {
      validation <- for {
        generalValidation <- validate(item)
        creationValidation <- validateForCreation(item)
      } yield generalValidation.merge(creationValidation)
      _ <- if (validation.isValid) {
        mongoRepository.create(item, username)
          .recover {
            case e: MongoWriteException =>
              throw ValidationException(Validation().withError("name", s"entity with name already exists: '${item.name}'"))
          }
      } else {
        throw ValidationException(validation)
      }
      detail <- getLatestVersion(item.name)
    } yield detail.map(d => (d, validation)) // valid validation may contain warnings
  }

  def recreate(username: String, item: C): Future[Option[(C, Validation)]] = {
    for {
      latestVersion <- getLatestVersionNumber(item.name)
      update <- update(username, item.setVersion(latestVersion).asInstanceOf[C])
    } yield update
  }

  def update(username: String, item: C): Future[Option[(C, Validation)]]

  private[services] def updateFuture(username: String, itemName: String, itemVersion: Int)(transform: C => Future[C]): Future[Option[(C, Validation)]] = {
    for {
      versionToUpdate <- getLatestVersion(itemName)
      (transformed, transformedValidation) <- if (versionToUpdate.isEmpty) {
        Future.failed(NotFoundException(s"Version $itemVersion of $itemName not found"))
      } else if (versionToUpdate.get.version != itemVersion) {
        Future.failed(ValidationException(Validation().withError("version", s"Version $itemVersion of $itemName is not the latest version, therefore cannot be edited")))
      } else if (versionToUpdate.get.lockedWithDefault) {
        Future.failed(LockedEntityException(s"Entity $itemName is locked"))
      } else {
        for {
          updatedEntity <- transform(versionToUpdate.get)
          validation <- validate(updatedEntity)
        } yield if (validation.isValid) {
          (updatedEntity, validation) // successful outcome, validation may still hold warnings
        } else {
          throw ValidationException(validation)
        }
      }
      update <- mongoRepository.update(username, transformed)
        .recover {
          case e: MongoWriteException =>
            throw ValidationException(Validation().withError("version", s"entity '$itemName' with this version already exists: ${itemVersion + 1}"))
        }
    } yield Some((update, transformedValidation))
  }

  private[services] def update(username: String, itemName: String, itemVersion: Int)(transform: C => C): Future[Option[(C, Validation)]] = {
    this.updateFuture(username, itemName, itemVersion) { item: C =>
      Future {
        transform(item)
      }
    }
  }

  def disableVersion(name: String, version: Option[Int]): Future[UpdateResult] = {
    val auth = SecurityContextHolder.getContext.getAuthentication
    val principal = auth.getPrincipal.asInstanceOf[UserDetails]

    getUsedIn(name, version).flatMap { usedR =>
      disableVersion(name, version, usedR, principal)
    }
  }

  private def disableVersion(name: String, version: Option[Int], usedIn: UsedIn, principal: UserDetails): Future[UpdateResult] = {
    if (usedIn.nonEmpty) {
      val entityVersionStr = s"""entity "$name"${ version.map(" v" + _).getOrElse("")}""" // either "entity MyName" or "entity MyName v23"
      throw EntityInUseException(s"""Cannot disable $entityVersionStr, because it is used in the following entities""", usedIn)
    } else {
      mongoRepository.disableVersion(name, version, principal.getUsername)
    }
  }

  def isDisabled(name: String): Future[Boolean] = {
    mongoRepository.isDisabled(name)
  }

  def setLock(name: String, isLocked: Boolean, principal: UserDetails): Future[UpdateResult] = {
    mongoRepository.setLockState(name, isLocked, principal.getUsername)
  }

  /**
   * Provides common validation (currently entity name validation). Override to extend for further specific validations.
   *
   * @param item
   * @return
   */
  def validate(item: C): Future[Validation] = {
    validateName(item.name)
  }

  /** does not include za.co.absa.enceladus.rest_api.services.VersionedModelService#validate(java.lang.Object)*/
  def validateForCreation(item: C): Future[Validation] = {
    isUniqueName(item.name).map { isUnique =>
      if (isUnique) {
        Validation.empty
      } else {
        Validation.empty.withError("name", s"entity with name already exists: '${item.name}'")
      }
    }
  }

  protected[services] def validateName(name: String): Future[Validation] = {
    if (hasWhitespace(name)) {
      Future.successful(Validation.empty.withError("name", s"name contains whitespace: '$name'"))
    } else {
      Future.successful(Validation.empty)
    }
  }

}

object VersionedModelService {
  private[services] def hasWhitespace(name: String): Boolean =
    Option(name).exists(definedName => !definedName.matches("""\w+"""))

  private[services] def hasValidNameChars(name: String): Boolean =
    Option(name).exists(definedName => definedName.matches("""[a-zA-Z0-9._-]+"""))

  private[services] def hasValidApiVersion(version: Option[String]): Boolean = version.contains(ModelVersion.toString)

}
