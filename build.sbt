name := "finagle-csp"

version := "1.0"

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url></url>
  <licenses>
    <license>
      <name></name>
      <url></url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url></url>
    <connection></connection>
  </scm>
  <developers>
    <developer>
      <id></id>
      <name></name>
      <url></url>
    </developer>
    <developer>
      <id></id>
      <name></name>
      <url></url>
    </developer>
  </developers>)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "http://maven.twttr.com"
)

libraryDependencies ++= Seq(
    "commons-codec" % "commons-codec" % "1.10",
    "com.twitter" %% "util-core" % "6.26.0",
    "eu.bitwalker" % "UserAgentUtils" % "1.16",
    "io.netty" % "netty" % "3.10.1.Final",
    "junit" % "junit" % "4.11" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0-M7" % "test",
    "com.twitter" %% "finagle-http" % "6.27.0" % "test"
  )
