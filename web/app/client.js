const {PrepareShapes, GetSomeShapes, ReleaseShapes} = require('./reactive-gateway_pb.js');
const {ShapeServiceClient} = require('./reactive-gateway_grpc_web_pb.js');
const {ShapesApp} = require('./shapesApp.js');
const grpc = {};
grpc.web = require('grpc-web');

var shapesService = new ShapeServiceClient('http://'+window.location.hostname+':8080', null, null);

var shapesApp = new ShapesApp(
  shapesService,
  {
    PrepareShapes: PrepareShapes,
    GetSomeShapes: GetSomeShapes,
    ReleaseShapes: ReleaseShapes
  },
  {
    checkGrpcStatusCode: function(status) {
      if (status.code != grpc.web.StatusCode.OK) {
        console.log('Error code: '+status.code+' "'+
                                status.details+'"');
      }
    }
  }
);

shapesApp.load();