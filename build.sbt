def commonSettings =
  Seq (
    copyrightHolder := "Yoppworks Inc.",
    startYear  := Some(2019),
    developerUrl := url("http://gitlab.com/yoppworks/reactive-gateway/"),
    titleForDocs := "Reactive Gateway",
    codePackage := "com.yoppworks.rxgateway",
    organization := "com.yoppworks",
    warningsAreErrors := false,
  )

lazy val api =
  (project in file("api"))
    .enablePlugins(ReactificPlugin)
    .settings(commonSettings)

lazy val server =
  (project in file("server"))
    .dependsOn(api)
    .enablePlugins(ReactificPlugin)
    .enablePlugins(AkkaGrpcPlugin)
    .enablePlugins(JavaAgent)
    .enablePlugins(JavaAppPackaging)
    .settings(commonSettings)

lazy val web =
  (project in file("web"))
    .dependsOn(api)
    .enablePlugins(ReactificPlugin)
    .settings(commonSettings)

lazy val mobile =
  (project in file("mobile"))
    .dependsOn(api)
    .enablePlugins(ReactificPlugin)
    .settings(commonSettings)

lazy val root = 
  (project in file(".")).aggregate(api, server, web, mobile)