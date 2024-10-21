/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, Json}

class AuthenticationResponseSpec extends SpecBase with ScalaCheckPropertyChecks {

  "AuthenticationResponse" - {

    "reads" - {
      "must deserialise" - {
        "when json is in expected shape" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (gatewayToken, affinityGroup) =>
              val json = Json.parse(s"""
                  |{
                  |  "gatewayToken" : "$gatewayToken",
                  |  "affinityGroup" : "$affinityGroup"
                  |}
                  |""".stripMargin)

              val result         = json.validate[AuthenticationResponse]
              val expectedResult = AuthenticationResponse(gatewayToken, affinityGroup)
              result.get.mustBe(expectedResult)
          }
        }
      }

      "must fail to deserialise" - {
        "when json is in unexpected shape" in {
          forAll(Gen.alphaNumStr) {
            gatewayToken =>
              val json = Json.parse(s"""
                   |{
                   |  "gatewayToken" : "$gatewayToken"
                   |}
                   |""".stripMargin)

              val result = json.validate[AuthenticationResponse]
              result.mustBe(a[JsError])
          }
        }
      }
    }
  }
}
