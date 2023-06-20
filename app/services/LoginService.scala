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

import connectors.ApiPlatformTestUserConnector
import models.{AuthenticatedSession, LoginRequest}
import org.joda.time.DateTime
import play.api.mvc.Session
import uk.gov.hmrc.http.SessionKeys._
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginService @Inject() (apiPlatformTestUserConnector: ApiPlatformTestUserConnector) {

  def authenticate(loginRequest: LoginRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Session] =
    apiPlatformTestUserConnector.authenticate(loginRequest) map buildSession

  private def buildSession(authSession: AuthenticatedSession): Session = Session(
    Map(
      sessionId            -> SessionId(s"session-${UUID.randomUUID}").value,
      authToken            -> authSession.authBearerToken,
      lastRequestTimestamp -> DateTime.now.getMillis.toString
    )
  )
}
