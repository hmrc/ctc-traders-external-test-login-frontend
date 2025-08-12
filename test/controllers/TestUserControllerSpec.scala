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

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.TestUser
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.TestUserService
import views.html.TestUserView

import scala.concurrent.Future

class TestUserControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val testUser = TestUser("userId", "password")

  private lazy val testUserRoute = routes.TestUserController.onPageLoad().url

  private lazy val mockTestUserService: TestUserService = mock[TestUserService]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(bind[TestUserService].toInstance(mockTestUserService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockTestUserService)
  }

  "TestUserController" - {

    "must return OK and the correct view for a GET" in {
      when(mockTestUserService.createUser(any())(any()))
        .thenReturn(Future.successful(testUser))

      val request = FakeRequest(GET, testUserRoute)

      val result = route(app, request).value

      val view = app.injector.instanceOf[TestUserView]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(testUser)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {
      val request = FakeRequest(POST, testUserRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.LoginController.onPageLoad().url
    }
  }
}
