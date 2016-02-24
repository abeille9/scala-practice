name := "ScalaPlay"

version := "1.0"

lazy val `semanticsna` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq( jdbc , cache , ws   ,
  "org.apache.spark"  %% "spark-core"              % "1.6.0",
  "org.apache.spark"  %% "spark-streaming"         % "1.6.0",
  "org.apache.spark"  %% "spark-streaming-twitter" % "1.6.0",
  "org.apache.spark"  %% "spark-mllib"             % "1.6.0",
  "edu.stanford.nlp"  %  "stanford-corenlp"        % "3.6.0",
  "edu.stanford.nlp"  %  "stanford-corenlp"        % "3.6.0" classifier "models",
  "com.datastax.spark" %% "spark-cassandra-connector" % "1.5.0-RC1",
  "com.github.etaty"  %% "rediscala" % "1.6.0",
  specs2 % Test )

dependencyOverrides ++= Set(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.4"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  