lazy val Versions = new {
  val kindProjector = "0.10.3"
  val scalamacros = "2.1.1"
  val zio = "1.0.0-RC12-1"
  val zioInteropCats = "2.0.0.0-RC2"
  val scalaTest = "3.0.8"
  val randomDataGenerator = "2.7"
  val logback = "1.2.3"
  val doobie = "0.8.0-RC1"
  val finch = "0.31.0"
  val jsonIgniter = "0.55.2"
  val pureconfig = "0.12.0"
}

ThisBuild / scalaVersion := "2.12.9"
ThisBuild / organization := "com.wantsome"
ThisBuild / scalafmtOnCompile := true
ThisBuild / fork in Test := true
ThisBuild / parallelExecution in Test := true
ThisBuild / turbo := true
ThisBuild / onChangedBuildSource := ReloadOnSourceChanges

addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector)
addCompilerPlugin("org.scalamacros" %% "paradise" % Versions.scalamacros cross CrossVersion.full)

lazy val commons = (project in file("commons")).settings(
  libraryDependencies ++= commonDeps,
  name := "commons"
)

lazy val testr = (project in file("testr")).settings(
  name := "testr",
  libraryDependencies ++= testrDeps
).dependsOn(commons)

lazy val verifyr = (project in file("verifyr")).settings(
  name := "verifyr",
  libraryDependencies ++= verifyrDeps
).dependsOn(commons)

enablePlugins(FlywayPlugin)

lazy val interactive = (project in file(".")).settings(
  name:= "interactive",
  flywayUrl := "jdbc:postgresql://localhost/postgres",
  flywayUser := "postgres",
  flywayPassword := "postgres",
  flywayLocations += "db/migrations",
  flywayUrl in Test:= "jdbc:postgresql://localhost/postgres",
  flywayUser in Test := "postgres",
  flywayPassword in Test := "postgres",
  flywaySchemas :=Seq("interactive"),
  libraryDependencies ++= interactiveDeps,
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
).configs(IntegrationTest).dependsOn(verifyr, testr)

// Scala libraries
val commonDeps = Seq(
  "dev.zio" %% "zio" % Versions.zio,
  "dev.zio" %% "zio-test-sbt" % Versions.zio,
  "dev.zio" %% "zio-test" % Versions.zio % "test",
  "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats,
  "ch.qos.logback" % "logback-classic" % Versions.logback,
  "org.tpolecat" %% "doobie-core" % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres" % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari" % Versions.doobie,
  "org.tpolecat" %% "doobie-quill" % Versions.doobie,
  "eu.timepit" %% "refined" % "0.9.9",
  "com.github.plokhotnyuk.jsoniter-scala"
    %% "jsoniter-scala-core"
    % Versions.jsonIgniter % Compile,
  "com.github.plokhotnyuk.jsoniter-scala"
    %% "jsoniter-scala-macros" %
    Versions.jsonIgniter % Provided,
"org.scalatest" %% "scalatest" % Versions.scalaTest % "test",
"com.danielasfregola"
  %% "random-data-generator"
  % Versions.randomDataGenerator % "test",
)

val testrDeps = Seq(
) ++ commonDeps

val verifyrDeps = Seq(
) ++ commonDeps

val interactiveDeps = Seq(
  "com.github.finagle" %% "finchx-core" % Versions.finch,
  "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig,
  "org.flywaydb" % "flyway-core" % "6.0.3" % "test"
) ++ commonDeps


