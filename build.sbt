name := "finagle-csp"

version := "1.0"

conflictWarning := ConflictWarning.disable

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Twitter Maven" at "http://maven.twttr.com",
  "Finatra Repo" at "http://twitter.github.com/finatra",
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
    "commons-codec" % "commons-codec" % "1.10",
    "com.twitter" % "util-core_2.9.1" % "4.0.1",
    "eu.bitwalker" % "UserAgentUtils" % "1.16",
    "io.netty" % "netty" % "3.10.1.Final",
    "junit" % "junit" % "4.11" % "test",
    "org.scalatest" % "scalatest_2.10" % "3.0.0-M7" % "test",
    "com.twitter" % "finagle-http_2.10" % "6.27.0" % "test"
  )
