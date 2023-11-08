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

import models._
import play.api.http.Status.CREATED
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApiPlatformTestUserConnector @Inject() (
  httpClient: HttpClient,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext) {
  private val serviceKey = "api-platform-test-user"

  val serviceUrl: String = {
    val context = servicesConfig.getConfString(s"$serviceKey.context", "")

    if (context.nonEmpty) {
      s"${servicesConfig.baseUrl(serviceKey)}/$context"
    } else {
      servicesConfig.baseUrl(serviceKey)
    }
  }

  def createTestUser(enrolments: Seq[String])(implicit hc: HeaderCarrier): Future[TestUser] = {
    val writes: Writes[Seq[String]] = Writes(
      x => Json.obj("serviceNames" -> x)
    )
    httpClient.POST[Seq[String], HttpResponse](s"$serviceUrl/organisations", enrolments)(writes, implicitly, implicitly, implicitly).map {
      response =>
        response.status match {
          case CREATED =>
            response.json.as[TestUser]
          case _ => throw new RuntimeException(s"Unexpected response code=${response.status} message=${response.body}")
        }
    }
  }

  def authenticate(login: Login)(implicit hc: HeaderCarrier): Future[AuthenticatedSession] =
    httpClient.POST[Login, Either[UpstreamErrorResponse, HttpResponse]](s"$serviceUrl/session", login) map {
      case Right(response) =>
        val authenticationResponse = response.json.as[AuthenticationResponse]

        (response.header(HeaderNames.AUTHORIZATION), response.header(HeaderNames.LOCATION)) match {
          case (Some(authBearerToken), Some(authorityUri)) =>
            AuthenticatedSession(
              authBearerToken,
              authorityUri,
              authenticationResponse.gatewayToken,
              authenticationResponse.affinityGroup
            )
          case _ => throw new RuntimeException("Authorization and Location headers must be present in response")
        }

      case Left(UpstreamErrorResponse(_, Status.UNAUTHORIZED, _, _)) => throw LoginFailedException(login.username)
      case Left(err)                                                 => throw err
    }
}
