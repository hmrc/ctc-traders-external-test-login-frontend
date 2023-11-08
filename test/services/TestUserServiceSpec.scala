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
import models.UserTypes.{AGENT, INDIVIDUAL, ORGANISATION}
import models.{Field, Service, TestIndividual}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.Future.successful

class TestUserServiceSpec extends SpecBase {

  private lazy val mockApiPlatformTestUserConnector = mock[ApiPlatformTestUserConnector]

  private val service1 = "service1"
  private val service2 = "service2"
  private val service3 = "service3"
  private val service4 = "service4"

  private val services = Seq(
    Service(service1, "Service 1", Seq(INDIVIDUAL)),
    Service(service2, "Service 2", Seq(INDIVIDUAL, ORGANISATION)),
    Service(service3, "Service 3", Seq(ORGANISATION)),
    Service(service4, "Service 4", Seq(AGENT))
  )

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
        // TODO - why does this only work with TestIndividual?
        val organisation = TestIndividual("org-user", "org-password", Seq(Field("saUtr", "Self Assessment UTR", "1555369053")))

        when(mockApiPlatformTestUserConnector.getServices()(any())).thenReturn(successful(services))
        when(mockApiPlatformTestUserConnector.createOrg(any())(any())).thenReturn(successful(organisation))

        val service = app.injector.instanceOf[TestUserService]

        val result = service.createUser(service3)

        result.futureValue mustBe organisation

        verify(mockApiPlatformTestUserConnector).createOrg(eqTo(Seq(service3)))(any())
      }
    }
  }

}
