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

package views

import play.api.data.Form
import play.api.i18n.Messages

object ViewUtils {

  def title(form: Form[?], title: String, section: Option[String] = None)(implicit messages: Messages): String =
    titleNoForm(
      title = if (form.hasErrors || form.hasGlobalErrors) {
        s"${messages("error.browser.title.prefix")} ${messages(title)}"
      } else {
        messages(title)
      },
      section = section
    )

  def titleNoForm(title: String, section: Option[String] = None)(implicit messages: Messages): String =
    Seq(
      Some(messages(title)),
      section.map(messages(_)),
      Some(messages("service.name")),
      Some(messages("site.govuk"))
    ).flatten.mkString(" - ")
}
