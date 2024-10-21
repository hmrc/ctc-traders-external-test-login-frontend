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

package views

import base.SpecBase
import forms.LoginFormProvider
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ViewUtilsSpec extends SpecBase with ScalaCheckPropertyChecks {

  "ViewUtils" - {
    "title" - {
      "must render title" - {
        "when form has errors" - {
          "and section is defined" in {
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (title, section) =>
                val formProvider   = new LoginFormProvider()
                val form           = formProvider.apply().bind(Map("userId" -> "", "password" -> ""))
                val result         = ViewUtils.title(form, title, Some(section))
                val expectedResult = s"Error: $title - $section - Manage your transit movements - GOV.UK"
                result.mustBe(expectedResult)
            }
          }

          "and section is undefined" in {
            forAll(Gen.alphaNumStr) {
              title =>
                val formProvider   = new LoginFormProvider()
                val form           = formProvider.apply().bind(Map("userId" -> "", "password" -> ""))
                val result         = ViewUtils.title(form, title, None)
                val expectedResult = s"Error: $title - Manage your transit movements - GOV.UK"
                result.mustBe(expectedResult)
            }
          }
        }

        "when form doesn't have errors" - {
          "and section is defined" in {
            forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
              (title, section) =>
                val formProvider   = new LoginFormProvider()
                val form           = formProvider.apply()
                val result         = ViewUtils.title(form, title, Some(section))
                val expectedResult = s"$title - $section - Manage your transit movements - GOV.UK"
                result.mustBe(expectedResult)
            }
          }

          "and section is undefined" in {
            forAll(Gen.alphaNumStr) {
              title =>
                val formProvider   = new LoginFormProvider()
                val form           = formProvider.apply()
                val result         = ViewUtils.title(form, title, None)
                val expectedResult = s"$title - Manage your transit movements - GOV.UK"
                result.mustBe(expectedResult)
            }
          }
        }
      }
    }

    "titleNoForm" - {
      "and section is defined" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (title, section) =>
            val result         = ViewUtils.titleNoForm(title, Some(section))
            val expectedResult = s"$title - $section - Manage your transit movements - GOV.UK"
            result.mustBe(expectedResult)
        }
      }

      "and section is undefined" in {
        forAll(Gen.alphaNumStr) {
          title =>
            val result         = ViewUtils.titleNoForm(title, None)
            val expectedResult = s"$title - Manage your transit movements - GOV.UK"
            result.mustBe(expectedResult)
        }
      }
    }
  }
}
