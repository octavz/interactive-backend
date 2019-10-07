lazy val Versions = new {
  val kindProjector = "0.10.3"
  val scalamacros = "2.1.1"
  val zio = "1.0.0-RC12-1"
  val zioInteropCats = "2.0.0.0-RC3"
  val randomDataGenerator = "2.7"
  val logback = "1.2.3"
  val doobie = "0.8.2"
  val finch = "0.31.0"
  val jsonIgniter = "0.55.2"
  val pureconfig = "0.12.0"
  val refined = "0.9.10"
  val circe = "0.11.1"
  val fintrospect="15.1.0"
}

ThisBuild / scalaVersion              := "2.12.10"
ThisBuild / organization              := "com.wantsome"
ThisBuild / scalafmtOnCompile         := true
ThisBuild / fork in Test              := true
ThisBuild / parallelExecution in Test := true
ThisBuild / turbo                     := true
ThisBuild / onChangedBuildSource      := ReloadOnSourceChanges

lazy val commons = (project in file("commons"))
  .configs(IntegrationTest)
  .settings(
    libraryDependencies ++= commonDeps,
    name := "commons",
    addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector),
    addCompilerPlugin("org.scalamacros" %% "paradise"     % Versions.scalamacros cross CrossVersion.full)
  )

lazy val testr = (project in file("testr"))
  .configs(IntegrationTest)
  .settings(
    name := "testr",
    libraryDependencies ++= testrDeps,
    Defaults.itSettings,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector),
    addCompilerPlugin("org.scalamacros" %% "paradise"     % Versions.scalamacros cross CrossVersion.full)
  )
  .dependsOn(commons)

lazy val verifyr = (project in file("verifyr"))
  .configs(IntegrationTest)
  .settings(
    name := "verifyr",
    libraryDependencies ++= verifyrDeps,
    Defaults.itSettings,
    addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector),
    addCompilerPlugin("org.scalamacros" %% "paradise"     % Versions.scalamacros cross CrossVersion.full)
  )
  .dependsOn(commons)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))

enablePlugins(FlywayPlugin)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    name           := "interactive",
    flywayUrl      := "jdbc:postgresql://localhost/postgres",
    flywayUser     := "postgres",
    flywayPassword := "postgres",
    flywayLocations += "db/migration",
    flywaySchemas := Seq("interactive"),
    libraryDependencies ++= interactiveDeps,
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
  .settings(Defaults.itSettings)
  .dependsOn(verifyr, testr)

// Scala libraries
val testDeps = Seq(
  "dev.zio" %% "zio-test"                          % Versions.zio                 % "test,it",
  "com.danielasfregola" %% "random-data-generator" % Versions.randomDataGenerator % "test,it",
  "com.dimafeng" %% "testcontainers-scala"         % "0.32.0"                     % "test,it",
  "org.testcontainers"                             % "postgresql"                 % "1.12.1" % "test,it"
)

val commonDeps = Seq(
  "dev.zio"               %% "zio"                  % Versions.zio,
  "dev.zio"               %% "zio-test-sbt"         % Versions.zio,
  "dev.zio"               %% "zio-interop-cats"     % Versions.zioInteropCats,
  "ch.qos.logback"        %  "logback-classic"      % Versions.logback,
  "org.tpolecat"          %% "doobie-core"          % Versions.doobie,
  "org.tpolecat"          %% "doobie-postgres"      % Versions.doobie,
  "org.tpolecat"          %% "doobie-hikari"        % Versions.doobie,
  "org.tpolecat"          %% "doobie-refined"       % Versions.doobie,
  "com.github.pureconfig" %% "pureconfig"           % Versions.pureconfig,
  "eu.timepit"            %% "refined"              % Versions.refined,
  "eu.timepit"            %% "refined-pureconfig"   % Versions.refined,
  "io.circe"              %% "circe-generic"        % Versions.circe,
  "io.circe"              %% "circe-refined"        % Versions.circe,
  "io.circe"              %% "circe-parser"         % Versions.circe,
  "org.flywaydb"          %  "flyway-core"          % "6.0.3",
  "com.github.mlangc"     %% "slf4zio"              % "0.2.1"

) ++ testDeps

val testrDeps = Seq(
  ) ++ commonDeps

val verifyrDeps = Seq(
  ) ++ commonDeps

val interactiveDeps = Seq(
  "com.github.finagle" %% "finchx-core"  % Versions.finch,
  "com.github.finagle" %% "finchx-circe" % Versions.finch,
  "io.fintrospect" %% "fintrospect-core" % Versions.fintrospect,
  "io.fintrospect" %% "fintrospect-circe" % Versions.fintrospect
) ++ commonDeps
