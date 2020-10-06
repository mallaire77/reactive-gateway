val Name =
  "rx-gateway-server"

name := Name

//codePackage := "com.yoppworks.rxgateway.server"

mainClass in (Compile, run) :=
  Some("com.yoppworks.rxgateway.server.ShapeGrpcServer")

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)

akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala)

akkaGrpcCodeGeneratorSettings += "server_power_apis"

// "sourceDirectory in Compile" is "src/main", so this adds "src/main/proto_custom":
inConfig(Compile)(
  Seq(
    PB.deleteTargetDirectory := false,
    PB.protoSources += baseDirectory.value / ".." / "api" / "src" / "main" / "proto"
  )
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-discovery"  % "2.5.31",
  "com.typesafe.akka" %% "akka-protobuf" % "2.5.31",
  "com.typesafe.akka" %% "akka-stream" % "2.5.31",
  "com.typesafe.akka" %% "akka-actor-typed"  % "2.5.31",
  "com.typesafe.akka" %% "akka-parsing" % "10.2.1",
  "com.typesafe.akka" %% "akka-http2-support" % "10.2.1",
  "com.typesafe.akka" %% "akka-http" % "10.2.1",
  "org.scalatest"     %% "scalatest" % "3.2.2" % "test",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.5.31" % Test
)

//Docker Build settings
dockerExposedPorts := Seq(9090)