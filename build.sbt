organization in ThisBuild := "com.thoughtworks.sbt-jdeps"

sbtPlugin := true

name := "sbt-jdeps"

libraryDependencies += "com.thoughtworks.dsl" %% "keywords-each" % "1.1.1"

addCompilerPlugin("com.thoughtworks.dsl" %% "compilerplugins-bangnotation" % "1.1.1")

addCompilerPlugin("com.thoughtworks.dsl" %% "compilerplugins-reseteverywhere" % "1.1.1")

addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.1.1")
