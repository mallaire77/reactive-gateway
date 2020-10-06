name := "rx-gateway-client"

mainClass in (Compile, run) :=
  Some("com.yoppworks.rxgateway.client.ShapeClient")

akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client)

akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala)

akkaGrpcCodeGeneratorSettings += "server_power_apis"

// "sourceDirectory in Compile" is "src/main", so this adds "src/main/proto":
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
