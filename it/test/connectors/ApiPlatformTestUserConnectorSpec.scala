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

import com.github.tomakehurst.wiremock.client.WireMock.*
import helpers.{ItSpecBase, WireMockServerHandler}
import models.*
import org.scalatest.Assertion
import play.api.http.HeaderNames.{AUTHORIZATION, LOCATION}
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.json.Json.toJson
import uk.gov.hmrc.http.UpstreamErrorResponse

class ApiPlatformTestUserConnectorSpec extends ItSpecBase with WireMockServerHandler {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure(conf = "microservice.services.api-platform-test-user.port" -> server.port())

  private lazy val connector: ApiPlatformTestUserConnector = app.injector.instanceOf[ApiPlatformTestUserConnector]

  private val login        = Login("user", "password")
  private val loginPayload = Json.toJson(login).toString

  "createTestUser" - {
    "should return a generated test user" in {
      val userId   = "user"
      val password = "password"

      val requestPayload =
        s"""{
           |  "serviceNames": [
           |    "national-insurance",
           |    "self-assessment",
           |    "mtd-income-tax"
           |  ]
           |}""".stripMargin

      server.stubFor(
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

      val result = connector.createTestUser(Seq("national-insurance", "self-assessment", "mtd-income-tax")).futureValue

      result.userId mustEqual userId
      result.password mustEqual password
    }

    "fail when api-platform-test-user returns a response that is not 201 CREATED" in {
      server.stubFor(
        post(urlEqualTo("/organisations"))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      val result = connector.createTestUser(Seq("national-insurance", "self-assessment", "mtd-income-tax"))

      whenReady[Throwable, Assertion](result.failed) {
        _ mustBe a[RuntimeException]
      }
    }
  }

  "authenticate" - {
    "should return the auth session when the credentials are valid" in {
      val authBearerToken = "Bearer AUTH_TOKEN"
      val userOid         = "/auth/oid/12345"
      val gatewayToken    = "GG_TOKEN"
      val affinityGroup   = "Individual"

      server.stubFor(
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

      val result = connector.authenticate(login).futureValue

      result mustEqual AuthenticatedSession(authBearerToken, userOid, gatewayToken, affinityGroup)
    }

    "should fail when expected headers are missing" in {
      val gatewayToken  = "GG_TOKEN"
      val affinityGroup = "Individual"

      server.stubFor(
        post(urlEqualTo("/session"))
          .withRequestBody(equalToJson(loginPayload))
          .willReturn(
            aResponse()
              .withStatus(CREATED)
              .withBody(Json.obj("gatewayToken" -> gatewayToken, "affinityGroup" -> affinityGroup).toString())
          )
      )

      val result = connector.authenticate(login)

      whenReady[Throwable, Assertion](result.failed) {
        _ mustBe a[RuntimeException]
      }
    }

    "should fail with LoginFailed when the credentials are not valid" in {
      server.stubFor(
        post(urlEqualTo("/session"))
          .withRequestBody(equalToJson(toJson(login).toString()))
          .willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
          )
      )

      val result = connector.authenticate(login)

      whenReady[Throwable, Assertion](result.failed) {
        _ mustBe a[LoginFailedException]
      }
    }

    "should fail when the authenticate call returns an error" in {
      server.stubFor(
        post(urlEqualTo("/session"))
          .withRequestBody(equalToJson(loginPayload))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.authenticate(login)

      whenReady[Throwable, Assertion](result.failed) {
        _ mustBe a[UpstreamErrorResponse]
      }
    }
  }
}
