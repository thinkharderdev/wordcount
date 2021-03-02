name := "wordcounter"

version := "0.1"

scalaVersion := "2.13.5"

inThisBuild(
  List(
    scalaVersion := "2.13.5",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

val fs2Version              = "2.5.0"
val circeVersion            = "0.12.3"
val http4sVersion           = "0.21.16"
val oslibVersion            = "0.7.3"
val munitVersion            = "0.7.22"
val munitCatsEffectVersion  = "0.13.1"
val scalacheckEffectVersion = "0.6.0"

idePackagePrefix := Some("dev.thinkharder")

scalacOptions ++= Seq("-Ywarn-unused", "-Yrangepos")

libraryDependencies ++= Seq(
  "com.lihaoyi"   %% "os-lib"                  % oslibVersion,
  "co.fs2"        %% "fs2-core"                % fs2Version,
  "co.fs2"        %% "fs2-io"                  % fs2Version,
  "io.circe"      %% "circe-core"              % circeVersion,
  "io.circe"      %% "circe-generic"           % circeVersion,
  "io.circe"      %% "circe-parser"            % circeVersion,
  "org.http4s"    %% "http4s-blaze-server"     % http4sVersion,
  "org.http4s"    %% "http4s-blaze-client"     % http4sVersion,
  "org.http4s"    %% "http4s-circe"            % http4sVersion,
  "org.http4s"    %% "http4s-dsl"              % http4sVersion,
  "org.scalameta" %% "munit"                   % munitVersion            % Test,
  "org.typelevel" %% "munit-cats-effect-2"     % munitCatsEffectVersion  % Test,
  "org.typelevel" %% "scalacheck-effect"       % scalacheckEffectVersion % Test,
  "org.typelevel" %% "scalacheck-effect-munit" % scalacheckEffectVersion % Test
)

testFrameworks += new TestFramework("munit.Framework")
