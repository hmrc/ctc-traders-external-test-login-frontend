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

package controllers

import config.FrontendAppConfig
import logger.ApplicationLogger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TestUserService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.TestUserView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestUserController @Inject() (
  override val messagesApi: MessagesApi,
  testUserService: TestUserService,
  messagesControllerComponents: MessagesControllerComponents,
  view: TestUserView
)(implicit val ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendController(messagesControllerComponents)
    with I18nSupport
    with ApplicationLogger {

  def onPageLoad(): Action[AnyContent] = Action.async {
    implicit request =>
      testUserService.createUser(config.serviceKey) map (
        user => Ok(view(user))
      )
  }

  def onSubmit(): Action[AnyContent] = Action {
    Redirect(routes.LoginController.onPageLoad())
  }
}
