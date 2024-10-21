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

class TestUserSpec extends SpecBase with ScalaCheckPropertyChecks {

  "TestUser" - {

    "reads" - {
      "must deserialise" - {
        "when json is in expected shape" in {
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (userId, password) =>
              val json = Json.parse(s"""
                  |{
                  |  "userId" : "$userId",
                  |  "password" : "$password"
                  |}
                  |""".stripMargin)

              val result         = json.validate[TestUser]
              val expectedResult = TestUser(userId, password)
              result.get.mustBe(expectedResult)
          }
        }
      }

      "must fail to deserialise" - {
        "when json is in unexpected shape" in {
          forAll(Gen.alphaNumStr) {
            userId =>
              val json = Json.parse(s"""
                   |{
                   |  "userId" : "$userId"
                   |}
                   |""".stripMargin)

              val result = json.validate[TestUser]
              result.mustBe(a[JsError])
          }
        }
      }
    }
  }
}
