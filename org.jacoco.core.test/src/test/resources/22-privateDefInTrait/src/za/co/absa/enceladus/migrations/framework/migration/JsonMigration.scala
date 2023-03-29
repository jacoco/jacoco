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

package za.co.absa.enceladus.migrations.framework.migration

import org.slf4j.{Logger, LoggerFactory}
import za.co.absa.enceladus.migrations.framework.dao.DocumentDb
import za.co.absa.enceladus.migrations.framework.{MigrationUtils, ObjectIdTools}

import scala.collection.mutable

/**
  * A JsonMigration represents an entity that provides transformations for each document of every collection in a model
  * when switching from one version of the model to another.
  *
  * All transformations are JSON string in, JSON string out.
  *
  * Only one transformation is possible per collection.
  *
  * In order to create a JSON migration you need to extend from this trait and provide all the requited transformations:
  *
  * {{{
  *   object MigrationTo1 extends MigrationBase with JsonMigration {
  *
  *     transformJSON("collection1_name") (jsonIn => {
  *       val jsonOut = collection1Transformations(jsonIn)
  *       jsonOut
  *     })
  *
  *     transformJSON("collection2_name") (jsonIn => {
  *       val jsonOut = collection2Transformations(jsonIn)
  *       jsonOut
  *     })
  *   }
  * }}}
  *
  * If a source document is invalid for some reason and should be dropped, just return `InvalidDocument`.
  */
trait JsonMigration extends Migration {

  // A document transformer is a function that maps a JSON string to a JSON string
  type DocumentTransformer = String => String

  private val log: Logger = LoggerFactory.getLogger(this.getClass)
  val InvalidDocument = ""

  private val transformers = new mutable.HashMap[String, DocumentTransformer]()

  /**
    * This function is used by derived classes to add transformations for affected collections.
    * This is used for complex migrations that requite complex model version maps.
    *
    * @param collectionName A collection name to be migrated
    * @param f              A transformation to applied to each document of the collection
    */
  def transformJSON(collectionName: String)(f: String => String): Unit = {
    if (transformers.contains(collectionName)) {
      throw new IllegalArgumentException(s"A transformer for '$collectionName' has already been added.")
    }
    transformers.put(collectionName, f)
  }

  /**
    * Gets a JSON transformer for the specified collection if applicable
    *
    * @param collectionName A collection name to be migrated
    * @return A function that takes a JSON string and returns a transformed JSON string
    */
  def getTransformer(collectionName: String): Option[DocumentTransformer] = transformers.get(collectionName)

  /**
    * Executes a migration on a given database and a list of collection names.
    */
  abstract override def execute(db: DocumentDb, collectionNames: Seq[String]): Unit = {
    super.execute(db, collectionNames)
    collectionNames.foreach(collection =>
      if (transformers.contains(collection)) {
        applyTransformers(db, collection)
      }
    )
  }

  /**
    * Validate the possibility of running a migration given a list of collection names.
    */
  abstract override def validate(collectionNames: Seq[String]): Unit = {
    super.validate(collectionNames)
    transformers.foreach {
      case (collectionToMigrate, _) => if (!collectionNames.contains(collectionToMigrate)) {
        throw new IllegalStateException(
          s"Attempt to apply a transform to a collection that does not exist: $collectionToMigrate.")
      }
    }
  }

  /**
    * Validate preconditions for this type of migration to run.
    */
  override protected def validateMigration(): Unit = {
    if (targetVersion <= 0) {
      throw new IllegalStateException("The target version of a JsonMigration should be greater than 0.")
    }
  }

  /**
    * Applies a transformer for each document of the collection to produce a migrated collection.
    */
  private def applyTransformers(db: DocumentDb, collectionName: String): Unit = {
    val sourceCollection = MigrationUtils.getVersionedCollectionName(collectionName, targetVersion - 1)
    val targetCollection = MigrationUtils.getVersionedCollectionName(collectionName, targetVersion)
    ensureCollectionEmpty(db, targetCollection)

    log.info(s"Applying a per-document transformation $sourceCollection -> $targetCollection")

    val transformer = transformers(collectionName)
    var invalidDocumentsCount = 0

    db.forEachDocument(sourceCollection)(doc => {
      val oid = ObjectIdTools.getObjectIdFromDocument(doc)
      val newDocument = ObjectIdTools.putObjectIdIfNotPresent(transformer(doc), oid)
      if (newDocument.nonEmpty) {
        db.insertDocument(targetCollection, newDocument)
      } else {
        invalidDocumentsCount += 1
        log.warn(s"Encountered a bogus document: $doc")
      }
    })
    logMigrationStatistics(db, sourceCollection, targetCollection, invalidDocumentsCount)
  }

  /**
    * Logs statistics about the number of migrated documents processed.
    */
  private def logMigrationStatistics(db: DocumentDb,
                                     sourceCollection: String,
                                     targetCollection: String,
                                     invalidDocumentsCount: Int): Unit = {
    val sourceDocsCnt = db.getDocumentsCount(sourceCollection)
    val targetDocsCnt = db.getDocumentsCount(targetCollection)
    if (invalidDocumentsCount == 0) {
      log.info(s"Migration '$sourceCollection' -> '$targetCollection' completed successfully. " +
        s"Total documents migrated: $targetDocsCnt.")
    } else if (invalidDocumentsCount == sourceDocsCnt) {
      log.error(s"Migration '$sourceCollection' -> '$targetCollection' failed. " +
        s"Unable to convert any of $sourceDocsCnt documents.")
    } else {
      log.warn(s"Migration '$sourceCollection' -> '$targetCollection' completed with errors. $targetDocsCnt out of " +
        s"$sourceDocsCnt were migrated. The number of invalid documents: $invalidDocumentsCount.")
    }
  }

  /**
    * Ensures a collection is empty.
    */
  private def ensureCollectionEmpty(db: DocumentDb, collectionName: String): Unit = {
    if (db.doesCollectionExists(collectionName)) {
      db.emptyCollection(collectionName)
    }
  }
}
