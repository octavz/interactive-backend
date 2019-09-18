lazy val Versions = new {
  val kindProjector = "0.10.3"
  val scalamacros = "2.1.1"
  val zio = "1.0.0-RC12-1"
  val zioInteropCats = "2.0.0.0-RC3"
  val scalaTest = "3.0.8"
  val randomDataGenerator = "2.7"
  val logback = "1.2.3"
  val doobie = "0.8.2"
  val finch = "0.31.0"
  val jsonIgniter = "0.55.2"
  val pureconfig = "0.12.0"
}

ThisBuild / scalaVersion              := "2.12.9"
ThisBuild / organization              := "com.wantsome"
ThisBuild / scalafmtOnCompile         := true
ThisBuild / fork in Test              := true
ThisBuild / parallelExecution in Test := true
ThisBuild / turbo                     := true
ThisBuild / onChangedBuildSource      := ReloadOnSourceChanges

addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector)
addCompilerPlugin("org.scalamacros" %% "paradise"     % Versions.scalamacros cross CrossVersion.full)

lazy val commons = (project in file("commons"))
  .configs(IntegrationTest)
  .settings(
    libraryDependencies ++= commonDeps,
    name := "commons"
  )

lazy val testr = (project in file("testr"))
  .configs(IntegrationTest)
  .settings(name := "testr", libraryDependencies ++= testrDeps)
  .settings(Defaults.itSettings)
  .dependsOn(commons)

lazy val verifyr = (project in file("verifyr"))
  .configs(IntegrationTest)
  .settings(name := "verifyr", libraryDependencies ++= verifyrDeps)
  .dependsOn(commons)
  .settings(testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")))
  .settings(Defaults.itSettings)

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
val commonDeps = Seq(
  "dev.zio" %% "zio"                  % Versions.zio,
  "dev.zio" %% "zio-test-sbt"         % Versions.zio,
  "dev.zio" %% "zio-test"             % Versions.zio % "test,it",
  "dev.zio" %% "zio-interop-cats"     % Versions.zioInteropCats,
  "ch.qos.logback"                    % "logback-classic" % Versions.logback,
  "org.tpolecat" %% "doobie-core"     % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres" % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari"   % Versions.doobie,
  "org.tpolecat" %% "doobie-quill"    % Versions.doobie,
  "eu.timepit" %% "refined"           % "0.9.10",
  "com.github.plokhotnyuk.jsoniter-scala"
    %% "jsoniter-scala-core"
    % Versions.jsonIgniter % Compile,
  "com.github.plokhotnyuk.jsoniter-scala"
    %% "jsoniter-scala-macros"   %
    Versions.jsonIgniter         % Provided,
  "org.scalatest" %% "scalatest" % Versions.scalaTest % "test,it",
  "com.danielasfregola"
    %% "random-data-generator"
    % Versions.randomDataGenerator         % "test,it",
  "org.flywaydb"                           % "flyway-core" % "6.0.3" % "test,it",
  "com.dimafeng" %% "testcontainers-scala" % "0.32.0" % "test,it",
  "org.testcontainers"                     % "postgresql" % "1.12.1" % "test,it"
)

val testrDeps = Seq(
  ) ++ commonDeps

val verifyrDeps = Seq(
  ) ++ commonDeps

val interactiveDeps = Seq(
  "com.github.finagle" %% "finchx-core"   % Versions.finch,
  "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig
) ++ commonDeps
