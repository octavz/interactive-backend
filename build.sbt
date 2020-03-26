lazy val Versions = new {
  val kindProjector = "0.11.0"
  val scalamacros = "2.1.1"
  val zio = "1.0.0-RC18-2"
  val zioInteropCats = "2.0.0.0-RC12"
  val logback = "1.2.3"
  val doobie = "0.8.8"
  val pureconfig = "0.12.3"
  val refined = "0.9.13"
  val circe = "0.13.0"
  val tapir = "0.12.25"
  val flyway = "6.3.1"
  val testcontainers = "0.36.1"
  val catsEffects = "2.1.1"
  val betterMonadicFor = "0.3.1"
  val zioLogging = "0.2.5"
}

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion      := "2.13.1"
ThisBuild / organization      := "com.wantsome"
ThisBuild / scalafmtOnCompile := true
ThisBuild / turbo             := false
ThisBuild / resolvers         ++= Seq(Opts.resolver.sonatypeSnapshots, Opts.resolver.sonatypeReleases)
ThisBuild / scalacOptions := Seq(
  "-Ywarn-unused",
  "-Ywarn-numeric-widen",
  "-deprecation",
  "-Ywarn-value-discard",
  "-Ymacro-annotations"
  //"-Xfatal-warnings"
)

IntegrationTest / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat

lazy val common = (project in file("common"))
  .configs(IntegrationTest)
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
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % Versions.betterMonadicFor)
  )
  .settings(Defaults.itSettings)
  .dependsOn(verifyr, testr)

// Scala libraries
val testDeps = Seq(
  "dev.zio"            %% "zio-test"                          % Versions.zio                    % "test,it",
  "dev.zio"            %% "zio-test-magnolia"                 % Versions.zio                    % "test,it",
  "com.dimafeng"       %% "testcontainers-scala"              % Versions.testcontainers         % "test,it",
  "com.dimafeng"       %% "testcontainers-scala-postgresql"   % Versions.testcontainers         % "test,it"
)

val commonDeps = Seq(
  "dev.zio"                     %% "zio"                      % Versions.zio,
  "dev.zio"                     %% "zio-test-sbt"             % Versions.zio,
  "dev.zio"                     %% "zio-interop-cats"         % Versions.zioInteropCats,
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
  "com.softwaremill.sttp.tapir" %% "tapir-core"               % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"      % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-json-circe"         % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-http4s"  % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-redoc-http4s"       % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"       % Versions.tapir,
  "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe-yaml" % Versions.tapir,
  "ch.qos.logback"              % "logback-classic"           % Versions.logback,
  "org.flywaydb"                % "flyway-core"               % Versions.flyway,
  "dev.zio"                     %% "zio-logging-slf4j"        % Versions.zioLogging 
) ++ testDeps

val testrDeps = Seq() ++ commonDeps

val verifyrDeps = Seq() ++ commonDeps

val interactiveDeps = Seq(
  "org.typelevel" %% "cats-effect" % Versions.catsEffects
) ++ commonDeps
