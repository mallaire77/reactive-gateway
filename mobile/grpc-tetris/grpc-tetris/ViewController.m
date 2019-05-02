//
//  ViewController.m
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-04-25.
//  Copyright © 2019 Yoppworks. All rights reserved.
//

#import "ViewController.h"
#import "Queue.h"
#import "ShapeFactory.h"
#import <UIKit/UIKit.h>
#import <GRPCClient/GRPCCall+ChannelArg.h>
#import <GRPCClient/GRPCCall+Tests.h>
#import <grpc-tetris/ReactiveGateway.pbrpc.h>

static NSString * const kHostAddress = @"localhost:50051";
static float REFRESH_INTERVAL = 2.0;

@interface ViewController ()
@property(atomic) Queue *queue;
@property(retain) ShapeFactory *shapeFactory;
@property(retain) UIView *lastShapeView;
@property (weak, nonatomic) IBOutlet UIView *shapeDrawingView;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.lastShapeView = nil;
    self.queue = [[Queue alloc] init];
    self.shapeFactory = [[ShapeFactory alloc] init];
    [NSTimer scheduledTimerWithTimeInterval:REFRESH_INTERVAL
                                     target:self
                                   selector:@selector(refreshShape)
                                   userInfo:nil
                                    repeats:YES];
}

-(void) refreshShape {
    NSLog(@"Refreshing Shape ...");
    if(!self.queue.isEmpty){
        TetrisShape *tetrisShape = self.queue.dequeue;
        NSLog(@"Creating Shape %d...",tetrisShape.shape.numberOfSides);
        UIView *view =[self.shapeFactory createWithShape:tetrisShape.shape];
        if(view!=nil){
            view.alpha = 0.5;
            view.center = CGPointMake(self.shapeDrawingView.frame.size.width  / 2,
                                      view.frame.size.height );
            [[NSOperationQueue mainQueue] addOperationWithBlock:^ {
                if(self.lastShapeView != nil){
                    [self.lastShapeView removeFromSuperview];
                }
                [self.shapeDrawingView addSubview:view];
                self.lastShapeView = view;
                float finalPosition = self.shapeDrawingView.frame.size.height - view.frame.size.height;
                [UIView animateWithDuration:1
                                 animations:^{
                                     view.alpha = 1.0;
                                     view.center = CGPointMake(self.shapeDrawingView.frame.size.width  / 2,
                                                               finalPosition);
                                 }
                                 completion:^(BOOL finished){
                                     
                }];
            }];
        }
    }
}

- (IBAction)onStartBtnClicked:(id)sender {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @autoreleasepool {
            NSLog(@"dispatching gRPC call");
            [GRPCCall useInsecureConnectionsForHost:kHostAddress];
            [GRPCCall setUserAgentPrefix:@"ShapeService/1.0" forHost:kHostAddress];
            ShapeService *client = [[ShapeService alloc] initWithHost:kHostAddress];
            GetSomeTetrisShapes *request = [GetSomeTetrisShapes message];
            request.startingIndex = 0;
            request.numberOfShapes = 3;
            request.dropSpotsArray = [GPBUInt32Array arrayWithValue:50];
            [client getSomeTetrisShapesWithRequest:request eventHandler:^(BOOL done,TetrisShape *response,NSError *error) {
                if(!done){
                    NSLog(@"Shape type : %d", response.shape.numberOfSides);
                    TetrisShape *tetrisShape = response;
                    [self.queue enqueue:tetrisShape];
                }
            }];
        }
    });
}

@end
