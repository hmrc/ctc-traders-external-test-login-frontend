/*
 * Copyright 2023 HM Revenue & Customs
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

package forms.mappings

import models.Login
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.collection.immutable.Seq

trait Formatters {

  private[mappings] def stringFormatter(errorKey: String, args: Any*): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def loginFormatter(requiredKey: String => String, args: Any*): Formatter[Login] = new Formatter[Login] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Login] = {
      def bind(key: String): Either[Seq[FormError], String] =
        stringFormatter(requiredKey(key), args*).bind(key, data)

      (bind("userId"), bind("password")) match {
        case (Right(userId), Right(password))         => Right(Login(userId, password))
        case (Left(userIdError), Left(passwordError)) => Left(userIdError ++ passwordError)
        case (Left(userIdError), _)                   => Left(userIdError)
        case (_, Left(passwordError))                 => Left(passwordError)
      }
    }

    override def unbind(key: String, value: Login): Map[String, String] =
      Map(
        "userId"   -> value.username,
        "password" -> value.password
      )
  }
}
