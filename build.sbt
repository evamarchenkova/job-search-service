ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

val catsVersion        = "2.9.0"
val catsEffect3        = "3.4.8"
val tapirVersion       = "1.7.6"
val http4sVersion      = "0.23.23"
val sttpClientVersion  = "3.9.1"
val catsBackendVersion = "3.9.1"
val ficusVersion       = "1.5.2"
val tethysVersion      = "0.26.0"
val pureConfigVersion  = "0.17.4"
val doobieVersion      = "1.0.0-RC2"
val quillVersion       = "4.6.0"
val flywayVersion = "9.16.0"
val catsRetryVersion = "3.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "job-search-service",
    libraryDependencies ++= Seq(
      // cats
      "org.typelevel" %% "cats-core"   % catsVersion,
      "org.typelevel" %% "cats-effect" % catsEffect3,

      // tapir
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server"     % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-tethys"       % tapirVersion,

      // http4s
      "org.http4s" %% "http4s-ember-server" % http4sVersion,

      // sttp
      "com.softwaremill.sttp.client3" %% "core"                           % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "circe"                          % sttpClientVersion,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % catsBackendVersion,

      // ficus
      "com.iheart" %% "ficus" % ficusVersion,

      // tethys
      "com.tethys-json" %% "tethys-core"       % tethysVersion,
      "com.tethys-json" %% "tethys-jackson"    % tethysVersion,
      "com.tethys-json" %% "tethys-derivation" % tethysVersion,
      "com.tethys-json" %% "tethys-enumeratum" % tethysVersion,

      // pureconfig
      "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,

      // doobie
      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % doobieVersion,

      // quill
      "io.getquill" %% "quill-doobie" % quillVersion,

      // flyway
      "org.flywaydb" % "flyway-core" % flywayVersion,

      // cats-retry
      "com.github.cb372" %% "cats-retry" % catsRetryVersion,
    ),
    addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1")
  )
  .enablePlugins(JavaAppPackaging)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    IntegrationTest / fork := true
  )
  .settings(
    Compile / run / fork := true
  )
