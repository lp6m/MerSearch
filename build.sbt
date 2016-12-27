name := "MerSearch"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.26",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  //"net.arnx" % "jsonic" % "1.3.9"
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.0"
)     


play.Project.playJavaSettings
