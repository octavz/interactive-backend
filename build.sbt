lazy val Versions = new {
  val kindProjector = "0.10.3"
  val scalamacros = "2.1.1"
  val zio = "1.0.0-RC12"
  val zioInteropCats = "2.0.0.0-RC2"
  val scalaTest = "3.0.8"
  val randomDataGenerator = "2.7"
  val logback = "1.2.3"
  val doobie = "0.8.0-RC1"
  val finch = "0.31.0"
  val jsonIgniter = "0.55.2"
}

ThisBuild / scalaVersion := "2.12.9"
ThisBuild / organization := "com.wantsome"
ThisBuild / scalafmtOnCompile := true
ThisBuild / fork in Test := true
ThisBuild / parallelExecution in Test := true

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

lazy val interactive = (project in file(".")).settings(
  name:= "interactive",
  libraryDependencies ++= interactiveDeps
).dependsOn(verifyr, testr)

// Scala libraries
val commonDeps = Seq(
  "dev.zio" %% "zio" % Versions.zio,
  "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats,
  "ch.qos.logback" % "logback-classic" % Versions.logback,
  "org.tpolecat" %% "doobie-core" % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres" % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari" % Versions.doobie,
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
  "com.github.pureconfig" %% "pureconfig" % "0.11.1"
) ++ commonDeps


