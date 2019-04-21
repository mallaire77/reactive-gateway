name := "rx-gateway-server"
codePackage := "com.yoppworks.rxgateway.server"

mainClass in (Compile, run) :=
  Some("com.yoppworks.rxgateway.server.ShapeServer")

enablePlugins(BuildInfoPlugin)
enablePlugins(AkkaGrpcPlugin)
enablePlugins(JavaAgent)
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test"

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)

akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala)

akkaGrpcCodeGeneratorSettings += "server_power_apis"

// "sourceDirectory in Compile" is "src/main", so this adds "src/main/proto_custom":
inConfig(Compile)(Seq(
  PB.protoSources += baseDirectory.value / ".." / "api" / "src" / "main" / "proto"
))

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-discovery"  % "2.5.20",
  "com.typesafe.akka" %% "akka-protobuf" % "2.5.20",
  "com.typesafe.akka" %% "akka-stream" % "2.5.20",
  "com.typesafe.akka" %% "akka-actor-typed"  % "2.5.20",
  "com.typesafe.akka" %% "akka-parsing" % "10.1.7",
  "com.typesafe.akka" %% "akka-http2-support" % "10.1.7",
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.5.20" % Test
)
