val commonSettings =
  Seq (
    copyrightHolder := "Yoppworks Inc.",
    startYear  := Some(2019),
    developerUrl := url("http://gitlab.com/yoppworks/reactive-gateway/"),
    titleForDocs := "Reactive Gateway",
    codePackage := "com.yoppworks.rxgateway",
    organization := "com.yoppworks",
    warningsAreErrors := false,
  )

val compileStageSetting =
  (_project: Project) =>
    (_project / Compile / compileIncremental) := ((_project / Compile / compileIncremental) dependsOn (_project / Compile / buildInfo)).value

lazy val api =
  (project in file("api"))
    .enablePlugins(ReactificPlugin)
    .settings(compileStageSetting(project))
    .settings(commonSettings)

lazy val server =
  (project in file("server"))
    .dependsOn(api)
    .enablePlugins(ReactificPlugin)
    .enablePlugins(BuildInfoPlugin)
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(AkkaGrpcPlugin)
    .enablePlugins(JavaAgent)
    .settings(compileStageSetting(project))
    .settings(commonSettings)

lazy val web =
  (project in file("web"))
    .dependsOn(api)
    .enablePlugins(ReactificPlugin)
    .settings(compileStageSetting(project))
    .settings(commonSettings)

lazy val mobile =
  (project in file("mobile"))
    .dependsOn(api)
    .enablePlugins(ReactificPlugin)
    .settings(compileStageSetting(project))
    .settings(commonSettings)

lazy val root = 
  (project in file(".")).aggregate(api, server, web, mobile)