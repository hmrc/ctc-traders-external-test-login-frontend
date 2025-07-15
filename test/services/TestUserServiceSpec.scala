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
import models.TestUser
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future.successful

class TestUserServiceSpec extends SpecBase {

  private lazy val mockApiPlatformTestUserConnector: ApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

  private val enrolments = Seq("common-transit-convention-traders")

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(
        bind[ApiPlatformTestUserConnector].toInstance(mockApiPlatformTestUserConnector)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockApiPlatformTestUserConnector)
  }

  "TestUserService" - {

    "createUser" - {
      "should return a generated organisation when type is ORGANISATION" in {
        val organisation = TestUser("org-user", "org-password")

        when(mockApiPlatformTestUserConnector.createTestUser(any())(any())).thenReturn(successful(organisation))

        val service = app.injector.instanceOf[TestUserService]

        val result = service.createUser(enrolments)

        result.futureValue mustEqual organisation

        verify(mockApiPlatformTestUserConnector).createTestUser(eqTo(enrolments))(any())
      }
    }
  }

}
