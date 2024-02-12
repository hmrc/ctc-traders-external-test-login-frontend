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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import helpers.ItSpecBase
import models._
import play.api.Application
import play.api.http.HeaderNames.{AUTHORIZATION, LOCATION}
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global

class ApiPlatformTestUserConnectorSpec extends ItSpecBase {

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .configure(("metrics.jvm", false))
      .build()

  private val login        = Login("user", "password")
  private val loginPayload = Json.toJson(login).toString

  trait Setup {
    implicit val hc = HeaderCarrier()

    val underTest = new ApiPlatformTestUserConnector(
      app.injector.instanceOf[HttpClient],
      app.injector.instanceOf[ServicesConfig]
    ) {
      override val serviceUrl: String = wireMockUrl
    }
  }

  "createTestUser" - {
    "should return a generated test user" in new Setup {
      private val userId   = "user"
      private val password = "password"

      val requestPayload =
        s"""{
           |  "serviceNames": [
           |    "national-insurance",
           |    "self-assessment",
           |    "mtd-income-tax"
           |  ]
           |}""".stripMargin

      stubFor(
        post(urlEqualTo("/organisations"))
          .withRequestBody(equalToJson(requestPayload))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
              .withBody(s"""
                           |{
                           |  "userId":"$userId",
                           |  "password":"$password",
                           |  "individualDetails": {
                           |    "firstName": "Ida",
                           |    "lastName": "Newton",
                           |    "dateOfBirth": "1960-06-01",
                           |    "address": {
                           |      "line1": "45 Springfield Rise",
                           |      "line2": "Glasgow",
                           |      "postcode": "TS1 1PA"
                           |    }
                           |  },
                           |  "eoriNumber":"1555369052"
                           |}""".stripMargin)
          )
      )

      val result = await(underTest.createTestUser(Seq("national-insurance", "self-assessment", "mtd-income-tax")))

      result.userId mustBe userId
      result.password mustBe password
    }

    "fail when api-platform-test-user returns a response that is not 201 CREATED" in new Setup {
      stubFor(
        post(urlEqualTo("/organisations"))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      intercept[RuntimeException] {
        await(underTest.createTestUser(Seq("national-insurance", "self-assessment", "mtd-income-tax")))
      }
    }
  }

  "authenticate" - {
    "should return the auth session when the credentials are valid" in new Setup {
      val authBearerToken = "Bearer AUTH_TOKEN"
      val userOid         = "/auth/oid/12345"
      val gatewayToken    = "GG_TOKEN"
      val affinityGroup   = "Individual"

      stubFor(
        post(urlEqualTo("/session"))
          .withRequestBody(equalToJson(loginPayload))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
              .withBody(Json.obj("gatewayToken" -> gatewayToken, "affinityGroup" -> affinityGroup).toString())
              .withHeader(AUTHORIZATION, authBearerToken)
              .withHeader(LOCATION, userOid)
          )
      )

      val result = await(underTest.authenticate(login))

      result mustBe AuthenticatedSession(authBearerToken, userOid, gatewayToken, affinityGroup)
    }

    "fail with LoginFailed when the credentials are not valid" in new Setup {
      stubFor(
        post(urlEqualTo("/session"))
          .withRequestBody(equalToJson(toJson(login).toString()))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )

      intercept[LoginFailedException] {
        await(underTest.authenticate(login))
      }
    }

    "fail when the authenticate call returns an error" in new Setup {
      stubFor(
        post(urlEqualTo("/session"))
          .withRequestBody(equalToJson(loginPayload))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      intercept[UpstreamErrorResponse] {
        await(underTest.authenticate(login))
      }.statusCode mustBe INTERNAL_SERVER_ERROR
    }

  }
}
