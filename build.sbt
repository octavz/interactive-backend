lazy val Versions = new {
  val kindProjector = "0.11.0"
  val scalamacros = "2.1.1"
  val zio = "1.0.0-RC17"
  val zioInteropCats = "2.0.0.0-RC10"
  val logback = "1.2.3"
  val doobie = "0.8.8"
  val pureconfig = "0.12.2"
  val refined = "0.9.10"
  val circe = "0.12.3"
  val tapir = "0.12.12"
  val flyway = "6.1.3"
  val zioLogging = "0.4.0"
  val testcontainers = "0.34.2"
}

//Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion      := "2.13.1"
ThisBuild / organization      := "com.wantsome"
ThisBuild / scalafmtOnCompile := true
ThisBuild / turbo             := false
ThisBuild / scalacOptions := Seq(
  "-Ywarn-unused",
  "-Ywarn-numeric-widen",
  "-deprecation",
  "-Ywarn-value-discard"
  //"-Xfatal-warnings"
)

IntegrationTest / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

lazy val common = (project in file("common"))
  .configs(IntegrationTest)
  .settings(resolvers += Resolver.sonatypeRepo("releases"))
  .settings(
    libraryDependencies ++= commonDeps,
    name := "common"
  )

lazy val testr = (project in file("testr"))
  .configs(IntegrationTest)
  .settings(
    name := "testr",
    libraryDependencies ++= testrDeps,
    Defaults.itSettings
  )
  .dependsOn(common)

lazy val verifyr = (project in file("verifyr"))
  .configs(IntegrationTest)
  .settings(
    name := "verifyr",
    libraryDependencies ++= verifyrDeps,
    Defaults.itSettings
  )
  .dependsOn(common)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name := "interactive",
    libraryDependencies ++= interactiveDeps,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
  .settings(Defaults.itSettings)
  .dependsOn(verifyr, testr)

// Scala libraries
val testDeps = Seq(
  "dev.zio"            %% "zio-test"             % Versions.zio            % "test,it",
  "com.dimafeng"       %% "testcontainers-scala" % Versions.testcontainers % "test,it",
  "org.testcontainers" % "postgresql"            % "1.12.4"                % "test,it"
)

val commonDeps = Seq(
  "dev.zio"                     %% "zio"                      % Versions.zio,
  "dev.zio"                     %% "zio-test-sbt"             % Versions.zio,
  "dev.zio"                     %% "zio-interop-cats"         % Versions.zioInteropCats,
  "ch.qos.logback"              % "logback-classic"           % Versions.logback,
  "org.tpolecat"                %% "doobie-core"              % Versions.doobie,
  "org.tpolecat"                %% "doobie-postgres"          % Versions.doobie,
  "org.tpolecat"                %% "doobie-hikari"            % Versions.doobie,
  "org.tpolecat"                %% "doobie-refined"           % Versions.doobie,
  "com.github.pureconfig"       %% "pureconfig"               % Versions.pureconfig,
  "eu.timepit"                  %% "refined"                  % Versions.refined,
  "eu.timepit"                  %% "refined-pureconfig"       % Versions.refined,
  "io.circe"                    %% "circe-generic"            % Versions.circe,
  "io.circe"                    %% "circe-refined"            % Versions.circe,
  "io.circe"                    %% "circe-parser"             % Versions.circe,
  "org.flywaydb"                % "flyway-core"               % Versions.flyway,
  "com.github.mlangc"           %% "slf4zio"                  % Versions.zioLogging,
  "com.softwaremill.sttp.tapir" %% "tapir-core"               % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-redoc-http4s"       % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % Versions.tapir
) ++ testDeps

val testrDeps = Seq(
  ) ++ commonDeps

val verifyrDeps = Seq(
  ) ++ commonDeps

val interactiveDeps = Seq(
  "org.typelevel" %% "cats-effect" % "2.0.0"
) ++ commonDeps
