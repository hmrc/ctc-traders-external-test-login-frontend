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

package services

import base.SpecBase
import connectors.ApiPlatformTestUserConnector
import models.{AuthenticatedSession, Login, LoginFailedException}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.{mock, reset, when}
import org.scalatest.Assertion
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Session

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LoginServiceSpec extends SpecBase {

  private val userId = "userId"
  private val login  = Login(userId, "password")

  private val uuid     = UUID.randomUUID()
  private val dateTime = DateTime.now()

  private val authBearerToken = "Bearer AUTH_TOKEN"

  private val authenticatedSession = AuthenticatedSession(authBearerToken, "/auth/oid/12345", "GG_TOKEN", "Individual")

  private lazy val mockApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]
  private lazy val mockUUIDService                  = mock[UUIDService]
  private lazy val mockDateTimeService              = mock[DateTimeService]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(
        bind[ApiPlatformTestUserConnector].toInstance(mockApiPlatformTestUserConnector),
        bind[UUIDService].toInstance(mockUUIDService),
        bind[DateTimeService].toInstance(mockDateTimeService)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiPlatformTestUserConnector)
    reset(mockUUIDService)
    reset(mockDateTimeService)
  }

  "LoginService" - {

    "must propagate LoginFailed exception when authentication fails" in {
      when(mockApiPlatformTestUserConnector.authenticate(any())(any())).thenReturn(Future.failed(LoginFailedException(userId)))

      val service = app.injector.instanceOf[LoginService]

      val result = service.authenticate(login)

      whenReady[Throwable, Assertion](result.failed) {
        _ mustBe a[LoginFailedException]
      }
    }

    "must build session when authentication succeeds" in {
      when(mockUUIDService.randomUUID).thenReturn(uuid)
      when(mockDateTimeService.now).thenReturn(dateTime)
      when(mockApiPlatformTestUserConnector.authenticate(any())(any())).thenReturn(Future.successful(authenticatedSession))

      val service = app.injector.instanceOf[LoginService]

      val result = service.authenticate(login)

      result.futureValue mustBe Session(
        Map(
          "sessionId" -> s"session-$uuid",
          "authToken" -> authBearerToken,
          "ts"        -> dateTime.getMillis.toString
        )
      )
    }
  }
}
