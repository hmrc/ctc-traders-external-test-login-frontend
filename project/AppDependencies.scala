import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.6.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"             % "10.3.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30"  % "2.0.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"             % bootstrapVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.scalatest"           %% "scalatest"               % "3.2.18",
    "org.mockito"             %  "mockito-core"            % "5.11.0",
    "org.scalatestplus"       %% "mockito-4-11"            % "3.2.18.0",
    "org.scalacheck"          %% "scalacheck"              % "1.18.0",
    "org.scalatestplus"       %% "scalacheck-1-17"         % "3.2.18.0",
    "org.jsoup"               %  "jsoup"                   % "1.17.2"
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
