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

package models

import models.UserTypes.ORGANISATION
import play.api.libs.json.{Format, JsError, JsSuccess, Writes, _}
import uk.gov.hmrc.play.json.Union

object EnumJson {

  def enumReads[E <: Enumeration](enumValue: E): Reads[E#Value] = {
    case JsString(s) =>
      try JsSuccess(enumValue.withName(s))
      catch {
        case _: NoSuchElementException =>
          JsError(s"Enumeration expected of type: '${enumValue.getClass}', but it does not contain '$s'")
      }
    case _ => JsError("String value expected")
  }

  def enumWrites[E <: Enumeration]: Writes[E#Value] = (v: E#Value) => JsString(v.toString)

  def enumFormat[E <: Enumeration](enumValue: E): Format[E#Value] =
    Format(enumReads(enumValue), enumWrites)

}

object JsonFormatters {
  implicit val formatCreateUserServicesRequest = Json.format[CreateUserRequest]
  implicit val formatUserType                  = EnumJson.enumFormat(UserTypes)
  implicit val formatService                   = Json.format[Service]

  implicit val formatField            = Json.format[Field]
  implicit val formatTestOrganisation = Json.format[TestOrganisation]

  implicit val formatTestUser: Format[TestUser] = Union
    .from[TestUser]("userType")
    .and[TestOrganisation](ORGANISATION.toString)
    .format

  implicit val formatAuthenticationResponse = Json.format[AuthenticationResponse]
}
