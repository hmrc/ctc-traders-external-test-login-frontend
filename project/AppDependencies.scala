import sbt._

object AppDependencies {
  import play.core.PlayVersion

  private val bootstrapVersion = "7.23.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"             % "7.26.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping"  % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"     % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-json-union-formatter"      % "1.18.0-play-28"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"  % bootstrapVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.15",
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.15.0",
    "org.scalatestplus"       %% "mockito-4-6"             % "3.2.15.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"      % "5.1.0",
    "org.pegdown"             %  "pegdown"                 % "1.6.0",
    "org.jsoup"               %  "jsoup"                   % "1.15.3",
    "com.typesafe.play"       %% "play-test"               % PlayVersion.current,
    "org.mockito"             %% "mockito-scala"           % "1.16.42",
    "org.scalacheck"          %% "scalacheck"              % "1.15.4",
    "com.vladsch.flexmark"    %  "flexmark-all"            % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
