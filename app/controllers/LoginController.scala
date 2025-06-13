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
import forms.LoginFormProvider
import models.{Login, LoginFailedException}
import play.api.data.{Form, FormError}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.LoginService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.LoginView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class LoginController @Inject() (
  loginService: LoginService,
  mcc: MessagesControllerComponents,
  formProvider: LoginFormProvider,
  view: LoginView
)(implicit val appConfig: FrontendAppConfig, val ec: ExecutionContext)
    extends FrontendController(mcc) {

  private val form: Form[Login] = formProvider()

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async {
    implicit request =>
      val boundForm = form.bindFromRequest()
      boundForm
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          login =>
            loginService.authenticate(login) map {
              session => Redirect(appConfig.continueUrl).withSession(session)
            } recover {
              case NonFatal(_: LoginFailedException) =>
                Unauthorized(view(boundForm.withError(FormError("value", "login.error.invalid"))))
            }
        )
  }
}
