ThisBuild / scalaVersion := "2.12.8"
ThisBuild / name := "interactive"
ThisBuild / organization := "com.wantsome"
ThisBuild / scalafmtOnCompile := true
ThisBuild / fork in Test := true
ThisBuild / parallelExecution in Test := true

lazy val Versions = new {
  val kindProjector = "0.10.3"
  val scalamacros = "2.1.1"
  val http4s = "0.20.10"
  val zio = "1.0.0-RC12"
  val zioInteropCats = "2.0.0.0-RC2"
  val circe = "0.11.1"
  val scalaTest = "3.0.8"
  val randomDataGenerator = "2.7"
  val ciris = "0.12.1"
  val logback = "1.2.3"
  val h2database = "1.4.199"
  val quill = "3.4.3"
  val tapir = "0.10.1"
  val doobie = "0.8.0-RC1"
  val finch = "0.26.0"
}

lazy val root = (project in file(".")).settings(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector),
  addCompilerPlugin("org.scalamacros" %% "paradise" % Versions.scalamacros cross CrossVersion.full),
  libraryDependencies ++= coreDeps
).dependsOn(verifyr)

lazy val verifyr = (project in file("verifyr")).settings(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % Versions.kindProjector),
  addCompilerPlugin("org.scalamacros" %% "paradise" % Versions.scalamacros cross CrossVersion.full),
  libraryDependencies ++= verifyrDeps
)

// Scala libraries
val commonDeps = Seq(
  "dev.zio" %% "zio" % Versions.zio,
  "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats,
  "ch.qos.logback" % "logback-classic" % Versions.logback,
  "org.tpolecat" % "doobie-core_2.12" % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres" % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari" % Versions.doobie,
  "io.circe" %% "circe-generic" % Versions.circe,
  "org.scalatest" %% "scalatest" % Versions.scalaTest % "test",
  "com.danielasfregola" %% "random-data-generator" % Versions.randomDataGenerator % "test"
)

val coreDeps = Seq(
  "org.http4s" %% "http4s-core" % Versions.http4s,
  "org.http4s" %% "http4s-dsl" % Versions.http4s,
  "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
  "org.http4s" %% "http4s-circe" % Versions.http4s,
  "io.getquill" %% "quill-jdbc" % Versions.quill,
  "is.cir" %% "ciris-cats" % Versions.ciris,
  "is.cir" %% "ciris-cats-effect" % Versions.ciris,
  "is.cir" %% "ciris-core" % Versions.ciris,
  "is.cir" %% "ciris-enumeratum" % Versions.ciris,
  "is.cir" %% "ciris-generic" % Versions.ciris,
  "is.cir" %% "ciris-refined" % Versions.ciris,
  "com.softwaremill.tapir" %% "tapir-core" % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-http4s-server" % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-swagger-ui-http4s" % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-openapi-docs" % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-openapi-circe-yaml" % Versions.tapir,
  "com.softwaremill.tapir" %% "tapir-json-circe" % Versions.tapir,
) ++ commonDeps

val verifyrDeps = Seq(
  "com.github.finagle" %% "finchx-core" % Versions.finch,
  "com.github.finagle" %% "finchx-circe" % Versions.finch,
) ++ commonDeps

