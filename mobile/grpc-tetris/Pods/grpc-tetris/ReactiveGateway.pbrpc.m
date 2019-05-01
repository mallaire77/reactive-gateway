#if !defined(GPB_GRPC_PROTOCOL_ONLY) || !GPB_GRPC_PROTOCOL_ONLY
#import "ReactiveGateway.pbrpc.h"
#import "ReactiveGateway.pbobjc.h"
#import <ProtoRPC/ProtoRPC.h>
#import <RxLibrary/GRXWriter+Immediate.h>


@implementation ShapeService

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wobjc-designated-initializers"

// Designated initializer
- (instancetype)initWithHost:(NSString *)host callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [super initWithHost:host
                 packageName:@"com.yoppworks.rxgateway.api"
                 serviceName:@"ShapeService"
                 callOptions:callOptions];
}

- (instancetype)initWithHost:(NSString *)host {
  return [super initWithHost:host
                 packageName:@"com.yoppworks.rxgateway.api"
                 serviceName:@"ShapeService"];
}

#pragma clang diagnostic pop

// Override superclass initializer to disallow different package and service names.
- (instancetype)initWithHost:(NSString *)host
                 packageName:(NSString *)packageName
                 serviceName:(NSString *)serviceName {
  return [self initWithHost:host];
}

- (instancetype)initWithHost:(NSString *)host
                 packageName:(NSString *)packageName
                 serviceName:(NSString *)serviceName
                 callOptions:(GRPCCallOptions *)callOptions {
  return [self initWithHost:host callOptions:callOptions];
}

#pragma mark - Class Methods

+ (instancetype)serviceWithHost:(NSString *)host {
  return [[self alloc] initWithHost:host];
}

+ (instancetype)serviceWithHost:(NSString *)host callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [[self alloc] initWithHost:host callOptions:callOptions];
}

#pragma mark - Method Implementations

#pragma mark prepareShapes(PrepareShapes) returns (ShapeServiceResult)

// Deprecated methods.
- (void)prepareShapesWithRequest:(PrepareShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler{
  [[self RPCToprepareShapesWithRequest:request handler:handler] start];
}
// Returns a not-yet-started RPC object.
- (GRPCProtoCall *)RPCToprepareShapesWithRequest:(PrepareShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler{
  return [self RPCToMethod:@"prepareShapes"
            requestsWriter:[GRXWriter writerWithValue:request]
             responseClass:[ShapeServiceResult class]
        responsesWriteable:[GRXWriteable writeableWithSingleHandler:handler]];
}
- (GRPCUnaryProtoCall *)prepareShapesWithMessage:(PrepareShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [self RPCToMethod:@"prepareShapes"
                   message:message
           responseHandler:handler
               callOptions:callOptions
             responseClass:[ShapeServiceResult class]];
}

#pragma mark getAShape(GetAShape) returns (ShapeServiceResult)

// Deprecated methods.
- (void)getAShapeWithRequest:(GetAShape *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler{
  [[self RPCTogetAShapeWithRequest:request handler:handler] start];
}
// Returns a not-yet-started RPC object.
- (GRPCProtoCall *)RPCTogetAShapeWithRequest:(GetAShape *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler{
  return [self RPCToMethod:@"getAShape"
            requestsWriter:[GRXWriter writerWithValue:request]
             responseClass:[ShapeServiceResult class]
        responsesWriteable:[GRXWriteable writeableWithSingleHandler:handler]];
}
- (GRPCUnaryProtoCall *)getAShapeWithMessage:(GetAShape *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [self RPCToMethod:@"getAShape"
                   message:message
           responseHandler:handler
               callOptions:callOptions
             responseClass:[ShapeServiceResult class]];
}

#pragma mark getSomeShapes(GetSomeShapes) returns (stream Shape)

// Deprecated methods.
- (void)getSomeShapesWithRequest:(GetSomeShapes *)request eventHandler:(void(^)(BOOL done, Shape *_Nullable response, NSError *_Nullable error))eventHandler{
  [[self RPCTogetSomeShapesWithRequest:request eventHandler:eventHandler] start];
}
// Returns a not-yet-started RPC object.
- (GRPCProtoCall *)RPCTogetSomeShapesWithRequest:(GetSomeShapes *)request eventHandler:(void(^)(BOOL done, Shape *_Nullable response, NSError *_Nullable error))eventHandler{
  return [self RPCToMethod:@"getSomeShapes"
            requestsWriter:[GRXWriter writerWithValue:request]
             responseClass:[Shape class]
        responsesWriteable:[GRXWriteable writeableWithEventHandler:eventHandler]];
}
- (GRPCUnaryProtoCall *)getSomeShapesWithMessage:(GetSomeShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [self RPCToMethod:@"getSomeShapes"
                   message:message
           responseHandler:handler
               callOptions:callOptions
             responseClass:[Shape class]];
}

#pragma mark getSomeTetrisShapes(GetSomeTetrisShapes) returns (stream TetrisShape)

// Deprecated methods.
- (void)getSomeTetrisShapesWithRequest:(GetSomeTetrisShapes *)request eventHandler:(void(^)(BOOL done, TetrisShape *_Nullable response, NSError *_Nullable error))eventHandler{
  [[self RPCTogetSomeTetrisShapesWithRequest:request eventHandler:eventHandler] start];
}
// Returns a not-yet-started RPC object.
- (GRPCProtoCall *)RPCTogetSomeTetrisShapesWithRequest:(GetSomeTetrisShapes *)request eventHandler:(void(^)(BOOL done, TetrisShape *_Nullable response, NSError *_Nullable error))eventHandler{
  return [self RPCToMethod:@"getSomeTetrisShapes"
            requestsWriter:[GRXWriter writerWithValue:request]
             responseClass:[TetrisShape class]
        responsesWriteable:[GRXWriteable writeableWithEventHandler:eventHandler]];
}
- (GRPCUnaryProtoCall *)getSomeTetrisShapesWithMessage:(GetSomeTetrisShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [self RPCToMethod:@"getSomeTetrisShapes"
                   message:message
           responseHandler:handler
               callOptions:callOptions
             responseClass:[TetrisShape class]];
}

#pragma mark releaseShapes(ReleaseShapes) returns (ShapeServiceResult)

// Deprecated methods.
- (void)releaseShapesWithRequest:(ReleaseShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler{
  [[self RPCToreleaseShapesWithRequest:request handler:handler] start];
}
// Returns a not-yet-started RPC object.
- (GRPCProtoCall *)RPCToreleaseShapesWithRequest:(ReleaseShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler{
  return [self RPCToMethod:@"releaseShapes"
            requestsWriter:[GRXWriter writerWithValue:request]
             responseClass:[ShapeServiceResult class]
        responsesWriteable:[GRXWriteable writeableWithSingleHandler:handler]];
}
- (GRPCUnaryProtoCall *)releaseShapesWithMessage:(ReleaseShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions {
  return [self RPCToMethod:@"releaseShapes"
                   message:message
           responseHandler:handler
               callOptions:callOptions
             responseClass:[ShapeServiceResult class]];
}

@end
#endif
