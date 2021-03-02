addSbtPlugin("org.jetbrains"       % "sbt-ide-settings"  % "1.1.0")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"      % "2.4.2")
addSbtPlugin("ch.epfl.scala"       % "sbt-scalafix"      % "0.9.26")
addCompilerPlugin(("org.scalameta" % "semanticdb-scalac" % "4.4.10").cross(CrossVersion.full))
