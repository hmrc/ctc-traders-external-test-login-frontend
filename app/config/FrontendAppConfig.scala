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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String             = configuration.get[String]("host")
  val appName: String          = configuration.get[String]("appName")
  val timeout: Int             = configuration.get[Int]("timeout-dialog.timeout")
  lazy val serviceKey: String  = configuration.get[String]("appName")
  lazy val continueUrl: String = configuration.get[String]("continue-url")
}
