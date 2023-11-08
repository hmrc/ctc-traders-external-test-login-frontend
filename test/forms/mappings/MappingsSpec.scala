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
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}

class MappingsSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {

  "login" - {

    val testForm: Form[Login] =
      Form(
        "value" -> login(
          x => s"error.$x.required"
        )
      )

    "must bind a valid login" in {
      val result = testForm.bind(Map("userId" -> "foo", "password" -> "bar"))
      result.get mustEqual Login("foo", "bar")
    }

    "must not bind an empty user ID" in {
      val result = testForm.bind(Map("userId" -> "", "password" -> "bar"))
      result.errors must contain(FormError("userId", "error.userId.required"))
    }

    "must not bind an empty password" in {
      val result = testForm.bind(Map("userId" -> "foo", "password" -> ""))
      result.errors must contain(FormError("password", "error.password.required"))
    }

    "must not bind an empty user ID password" in {
      val result = testForm.bind(Map("userId" -> "", "password" -> ""))
      result.errors must contain(FormError("userId", "error.userId.required"))
      result.errors must contain(FormError("password", "error.password.required"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill(Login("foo", "bar"))
      result.apply("userId").value.value mustEqual "foo"
      result.apply("password").value.value mustEqual "bar"
    }
  }
}
