name := "OWLOnsite"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.4"
libraryDependencies += "junit" % "junit" % "4.12" % Test
libraryDependencies += "org.mockito" % "mockito-all" % "1.9.5" % Test
libraryDependencies += "org.jmock" % "jmock-junit4" % "2.8.3" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % Test