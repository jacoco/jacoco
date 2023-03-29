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

package za.co.absa.commons.error

import org.slf4j.LoggerFactory

import java.util.UUID

/**
  * <p>
  * An unexpected exception wrapper that is aimed for being sent to a client as a result of a failed operation.
  * `ErrorRef` holds a unique identifier (UUID) with which the exception is logged to the server logs.
  * It may optionally also take an error message dedicated for a client, that may be different from the one of the original exception.
  * </p>
  * <p>
  * This way the error message that the client receives can be matched with the corresponding server-side error later
  * for the ease of troubleshooting.
  * </p>
  *
  * <h1>Usage example:</h1>
  *
  * <p>
  * When an unexpected error occurs during processing a client request, instead of propagating the exception
  * to the client verbatim, create an `ErrorRef` instance (e.g. via `ErrorRef.apply()`
  * and return it as a response body along with an appropriate response status code (e.g. 500).
  * </p>
  */
case class ErrorRef(
  errorId: UUID,
  timestamp: Long,
  message: Option[String]
)

object ErrorRef extends ErrorRefFactory(LoggerFactory.getLogger(classOf[ErrorRef])) {
  def apply(e: Throwable, msg: String = null): ErrorRef = createRef(e, Option(msg))
}
