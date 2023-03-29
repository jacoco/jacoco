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

package za.co.absa.enceladus.dao

import scala.util.{Success, Failure, Try}

abstract class RestApiException(message: String, cause: Throwable) extends Exception(message, cause)

abstract class NotRetryableException(message: String, cause: Throwable) extends RestApiException(message, cause)

object NotRetryableException {

  final case class AuthenticationException(private val message: String,
                                           private val cause: Throwable = None.orNull)
    extends NotRetryableException(message, cause)
}

abstract class RetryableException(message: String, cause: Throwable) extends RestApiException(message, cause)

object RetryableException {

  final case class DaoException(private val message: String,
                                private val cause: Throwable = None.orNull)
    extends RetryableException(message, cause)

  final case class AutoRecoverableException(private val message: String,
                                            private val cause: Throwable = None.orNull)
    extends RetryableException(message, cause)
}

abstract class OptionallyRetryableException(message: String, cause: Throwable) extends RestApiException(message, cause)

object OptionallyRetryableException {

  type OptRetryableExceptions = Class[_ <: OptionallyRetryableException]

  final case class UnauthorizedException(private val message: String,
                                         private val cause: Throwable = None.orNull)
    extends OptionallyRetryableException(message, cause)

  final case class ForbiddenException(private val message: String,
                                      private val cause: Throwable = None.orNull)
    extends OptionallyRetryableException(message, cause)

  final case class NotFoundException(private val message: String,
                                     private val cause: Throwable = None.orNull)
    extends OptionallyRetryableException(message, cause)

  val mapIntToOptionallyRetryableException: Map[Int, OptRetryableExceptions] = Map(
    401 -> classOf[UnauthorizedException],
    403 -> classOf[ForbiddenException],
    404 -> classOf[NotFoundException]
  )

  /**
    * This function returns a custom exception class from the input HTTP status code. If the HTTP status code is not
    * supported, then an exception will be raised.
    *
    *  @param httpStatusCode an integer representation of HTTP status code that should be converted
    *                       to a custom class representing it.
    */
  def getOptionallyRetryableException(httpStatusCode: Int): OptRetryableExceptions = {
    Try(mapIntToOptionallyRetryableException(httpStatusCode)) match {
      case Success(retryableException) => retryableException
      case Failure(exception) =>
        throw new IllegalArgumentException(
          s"The input HTTP status code '$httpStatusCode' is not supported. " +
          s"These are supported: ${mapIntToOptionallyRetryableException.keys.mkString("[", ", ", "]")} ",
          exception
        )
    }
  }
}
