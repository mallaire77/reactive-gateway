val commonSettings = Seq(
  copyrightHolder := "Yoppworks Inc.",
  startYear  := Some(2019),
  developerUrl := url("http://gitlab.com/yoppworks/reactive-gateway/"),
  titleForDocs := "Reactive Gateway",
  codePackage := "com.yoppworks.rxgateway"
)

lazy val api = (project in file("api"))
  .enablePlugins(ReactificPlugin)
  .settings(commonSettings)


lazy val server = (project in file("server"))
  .enablePlugins(ReactificPlugin)
  .dependsOn(api)
  .settings(commonSettings)

lazy val web = (project in file("web"))
  .enablePlugins(ReactificPlugin)
  .dependsOn(api)
  .settings(commonSettings)


lazy val mobile = (project in file("mobile"))
  .enablePlugins(ReactificPlugin)
  .dependsOn(api)
  .settings(commonSettings)
