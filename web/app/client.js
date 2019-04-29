const {GetSomeShapes} = require('./reactive-gateway_pb.js');
const {ShapeServiceClient} = require('./reactive-gateway_grpc_web_pb.js');

const grpc = {};
grpc.web = require('grpc-web');

var shapeService = new ShapeServiceClient('http://'+window.location.hostname+':8080', null, null);

var request = new GetSomeShapes();
request.setNumberofshapes(3);


shapeService.getSomeShapes(request, {}, function(err, response){
  console.log('Response', response);
  console.log('Error :', response);
});


