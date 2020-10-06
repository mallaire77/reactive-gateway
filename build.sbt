def commonSettings =
  Seq (
    scalaVersion := "2.13.1",
    startYear  := Some(2019),
    organization := "com.yoppworks",
  )

lazy val api =
  (project in file("api"))
    .settings(commonSettings)

lazy val client =
  (project in file("client"))
    .dependsOn(api)
    .enablePlugins(AkkaGrpcPlugin)
    .enablePlugins(JavaAppPackaging)
    .settings(commonSettings)

lazy val server =
  (project in file("server"))
    .dependsOn(api)
    .enablePlugins(AkkaGrpcPlugin)
    .enablePlugins(JavaAppPackaging)
    .settings(commonSettings)

lazy val web =
  (project in file("web"))
    .dependsOn(api)
    .settings(commonSettings)

lazy val mobile =
  (project in file("mobile"))
    .dependsOn(api)
    .settings(commonSettings)

lazy val root = 
  (project in file("."))
    .settings(name := "reactive-gateway")
    .aggregate(api, client, server, web, mobile)