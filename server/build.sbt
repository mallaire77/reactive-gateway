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
