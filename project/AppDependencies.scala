import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.12.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"             % "12.6.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30"  % "3.3.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"             % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.19",
    "org.mockito"             %  "mockito-core"            % "5.14.2",
    "org.scalatestplus"       %% "mockito-5-12"            % "3.2.19.0",
    "org.scalacheck"          %% "scalacheck"              % "1.18.1",
    "org.scalatestplus"       %% "scalacheck-1-18"         % "3.2.19.0",
    "org.jsoup"               %  "jsoup"                   % "1.18.1"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
