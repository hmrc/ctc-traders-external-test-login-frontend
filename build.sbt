import play.sbt.routes.RoutesKeys
import sbt.ThisBuild
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings

lazy val appName: String = "ctc-traders-external-test-login-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.5.0"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models._",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.html.templates._",
      "views.ViewUtils._",
      "controllers.routes"
    ),
    PlayKeys.playDefaultPort := 19154,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration",
    ScoverageKeys.coverageExcludedPackages := Seq(
      ".*views.html.components.*",
      ".*views.html.templates.*"
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting  := true,
    scalacOptions ++= Seq(
      "-feature",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:higherKinds",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=html/.*:s",
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    Assets / pipelineStages := Seq(digest)
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(
    libraryDependencies ++= AppDependencies.test,
    DefaultBuildSettings.itSettings()
  )
