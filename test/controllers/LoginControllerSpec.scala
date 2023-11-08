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

import base.SpecBase
import forms.LoginFormProvider
import models.{Login, LoginFailedException}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import play.api.data.FormError
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.LoginService
import views.html.LoginView

import scala.concurrent.Future

class LoginControllerSpec extends SpecBase {

  private val formProvider = new LoginFormProvider()
  private val form         = formProvider()

  private val userId   = "userId"
  private val password = "password"
  private val session  = Session(Map("authBearerToken" -> "Bearer AUTH_TOKEN"))

  private lazy val loginRoute = routes.LoginController.onPageLoad().url

  private lazy val mockLoginService: LoginService = mock[LoginService]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(bind[LoginService].toInstance(mockLoginService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLoginService)
  }

  "LoginController" - {

    "must return OK and the correct view for a GET" in {
      val request = FakeRequest(GET, loginRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[LoginView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      when(mockLoginService.authenticate(any())(any(), any()))
        .thenReturn(Future.successful(session))

      val request = FakeRequest(POST, loginRoute)
        .withFormUrlEncodedBody(
          "userId"   -> userId,
          "password" -> password
        )

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustBe frontendAppConfig.continueUrl

      verify(mockLoginService).authenticate(eqTo(Login(userId, password)))(any(), any())
    }

    "must return Unauthorized and errors when invalid data is submitted" in {
      when(mockLoginService.authenticate(any())(any(), any()))
        .thenReturn(Future.failed(LoginFailedException("")))

      val filledForm = form
        .bind(Map("userId" -> userId, "password" -> password))
        .withError(FormError("value", "login.error.invalid"))

      val request = FakeRequest(POST, loginRoute)
        .withFormUrlEncodedBody("userId" -> userId, "password" -> password)

      val result = route(app, request).value

      val view = app.injector.instanceOf[LoginView]

      status(result) mustEqual UNAUTHORIZED

      contentAsString(result) mustEqual
        view(filledForm)(request, messages).toString
    }

    "must return BadRequest and errors when empty data is submitted" in {
      when(mockLoginService.authenticate(any())(any(), any()))
        .thenReturn(Future.failed(LoginFailedException("")))

      val invalidAnswer = ""

      val filledForm = form
        .bind(Map("userId" -> invalidAnswer, "password" -> invalidAnswer))

      val request = FakeRequest(POST, loginRoute)
        .withFormUrlEncodedBody("userId" -> invalidAnswer, "password" -> invalidAnswer)

      val result = route(app, request).value

      val view = app.injector.instanceOf[LoginView]

      status(result) mustEqual BAD_REQUEST

      contentAsString(result) mustEqual
        view(filledForm)(request, messages).toString
    }
  }
}
