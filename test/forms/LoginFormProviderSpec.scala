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

package forms

import base.SpecBase
import models.Login
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}

class LoginFormProviderSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val form: Form[Login] = new LoginFormProvider()()

  "LoginFormProvider" - {

    "must bind valid data" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (userId, password) =>
          val result = form.bind(Map("userId" -> userId, "password" -> password))
          result("userId").value.value mustEqual userId
          result("password").value.value mustEqual password
      }
    }

    "must not bind blank user ID" in {
      val result = form.bind(Map("userId" -> "")).apply("userId")
      result.errors mustEqual Seq(FormError("userId", "login.userId.error.required"))
    }

    "must not bind whitespace user ID" in {
      val result = form.bind(Map("userId" -> " ")).apply("userId")
      result.errors mustEqual Seq(FormError("userId", "login.userId.error.required"))
    }

    "must not bind blank password" in {
      val result = form.bind(Map("password" -> "")).apply("password")
      result.errors mustEqual Seq(FormError("password", "login.password.error.required"))
    }

    "must not bind whitespace password" in {
      val result = form.bind(Map("password" -> " ")).apply("password")
      result.errors mustEqual Seq(FormError("password", "login.password.error.required"))
    }
  }
}
