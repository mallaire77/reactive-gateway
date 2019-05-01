#if !defined(GPB_GRPC_FORWARD_DECLARE_MESSAGE_PROTO) || !GPB_GRPC_FORWARD_DECLARE_MESSAGE_PROTO
#import "ReactiveGateway.pbobjc.h"
#endif

#if !defined(GPB_GRPC_PROTOCOL_ONLY) || !GPB_GRPC_PROTOCOL_ONLY
#import <ProtoRPC/ProtoService.h>
#import <ProtoRPC/ProtoRPC.h>
#import <RxLibrary/GRXWriteable.h>
#import <RxLibrary/GRXWriter.h>
#endif

@class GetAShape;
@class GetSomeShapes;
@class GetSomeTetrisShapes;
@class PrepareShapes;
@class ReleaseShapes;
@class Shape;
@class ShapeServiceResult;
@class TetrisShape;

#if !defined(GPB_GRPC_FORWARD_DECLARE_MESSAGE_PROTO) || !GPB_GRPC_FORWARD_DECLARE_MESSAGE_PROTO
#endif

@class GRPCProtoCall;
@class GRPCUnaryProtoCall;
@class GRPCStreamingProtoCall;
@class GRPCCallOptions;
@protocol GRPCProtoResponseHandler;


NS_ASSUME_NONNULL_BEGIN

@protocol ShapeService2 <NSObject>

#pragma mark prepareShapes(PrepareShapes) returns (ShapeServiceResult)

- (GRPCUnaryProtoCall *)prepareShapesWithMessage:(PrepareShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions;

#pragma mark getAShape(GetAShape) returns (ShapeServiceResult)

- (GRPCUnaryProtoCall *)getAShapeWithMessage:(GetAShape *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions;

#pragma mark getSomeShapes(GetSomeShapes) returns (stream Shape)

- (GRPCUnaryProtoCall *)getSomeShapesWithMessage:(GetSomeShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions;

#pragma mark getSomeTetrisShapes(GetSomeTetrisShapes) returns (stream TetrisShape)

- (GRPCUnaryProtoCall *)getSomeTetrisShapesWithMessage:(GetSomeTetrisShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions;

#pragma mark releaseShapes(ReleaseShapes) returns (ShapeServiceResult)

- (GRPCUnaryProtoCall *)releaseShapesWithMessage:(ReleaseShapes *)message responseHandler:(id<GRPCProtoResponseHandler>)handler callOptions:(GRPCCallOptions *_Nullable)callOptions;

@end

@protocol ShapeService <NSObject>

#pragma mark prepareShapes(PrepareShapes) returns (ShapeServiceResult)

- (void)prepareShapesWithRequest:(PrepareShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler;

- (GRPCProtoCall *)RPCToprepareShapesWithRequest:(PrepareShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler;


#pragma mark getAShape(GetAShape) returns (ShapeServiceResult)

- (void)getAShapeWithRequest:(GetAShape *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler;

- (GRPCProtoCall *)RPCTogetAShapeWithRequest:(GetAShape *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler;


#pragma mark getSomeShapes(GetSomeShapes) returns (stream Shape)

- (void)getSomeShapesWithRequest:(GetSomeShapes *)request eventHandler:(void(^)(BOOL done, Shape *_Nullable response, NSError *_Nullable error))eventHandler;

- (GRPCProtoCall *)RPCTogetSomeShapesWithRequest:(GetSomeShapes *)request eventHandler:(void(^)(BOOL done, Shape *_Nullable response, NSError *_Nullable error))eventHandler;


#pragma mark getSomeTetrisShapes(GetSomeTetrisShapes) returns (stream TetrisShape)

- (void)getSomeTetrisShapesWithRequest:(GetSomeTetrisShapes *)request eventHandler:(void(^)(BOOL done, TetrisShape *_Nullable response, NSError *_Nullable error))eventHandler;

- (GRPCProtoCall *)RPCTogetSomeTetrisShapesWithRequest:(GetSomeTetrisShapes *)request eventHandler:(void(^)(BOOL done, TetrisShape *_Nullable response, NSError *_Nullable error))eventHandler;


#pragma mark releaseShapes(ReleaseShapes) returns (ShapeServiceResult)

- (void)releaseShapesWithRequest:(ReleaseShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler;

- (GRPCProtoCall *)RPCToreleaseShapesWithRequest:(ReleaseShapes *)request handler:(void(^)(ShapeServiceResult *_Nullable response, NSError *_Nullable error))handler;


@end


#if !defined(GPB_GRPC_PROTOCOL_ONLY) || !GPB_GRPC_PROTOCOL_ONLY
/**
 * Basic service implementation, over gRPC, that only does
 * marshalling and parsing.
 */
@interface ShapeService : GRPCProtoService<ShapeService, ShapeService2>
- (instancetype)initWithHost:(NSString *)host callOptions:(GRPCCallOptions *_Nullable)callOptions NS_DESIGNATED_INITIALIZER;
- (instancetype)initWithHost:(NSString *)host;
+ (instancetype)serviceWithHost:(NSString *)host callOptions:(GRPCCallOptions *_Nullable)callOptions;
+ (instancetype)serviceWithHost:(NSString *)host;
@end
#endif

NS_ASSUME_NONNULL_END

