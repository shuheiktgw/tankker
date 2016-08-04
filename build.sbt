name := "Tankker"

version := "1.0"

lazy val `tankker` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  ws,
  specs2 % Test,
  "jp.t2v" %% "play2-auth"        % "0.14.2",
  "jp.t2v" %% "play2-auth-social" % "0.14.2",
  "jp.t2v" %% "play2-auth-test"   % "0.14.2" % "test",
  play.sbt.Play.autoImport.cache,
  "com.github.t3hnar" %% "scala-bcrypt" % "2.6",
  "com.typesafe.slick" %% "slick-codegen" % "3.1.0",
  "com.typesafe.play" %% "play-slick" % "2.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
  "mysql" % "mysql-connector-java" % "5.1.36"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
