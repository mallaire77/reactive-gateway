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
static NSString* const NUMBER_OF_SHAPES_LBL = @"Number of Shapes : %d";
@interface ViewController ()
@property (weak, nonatomic) IBOutlet UIButton *requestButton;
@property(retain) NSNumber *numberOfShapes;
@property(atomic) Queue *queue;
@property(atomic) NSInteger *pendingShapesToBeDraw;
@property(retain) ShapeFactory *shapeFactory;
@property(retain) UIView *lastShapeView;
@property (weak, nonatomic) IBOutlet UIView *shapeDrawingView;
@property (weak, nonatomic) IBOutlet UILabel *numberOfShapesLabel;
@property (weak, nonatomic) IBOutlet UISlider *numberOfShapesSlider;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setNumberOfShapesToRequest:[NSNumber numberWithInt:1]];
    [self.numberOfShapesSlider setContinuous:false];
    self.lastShapeView = nil;
    self.pendingShapesToBeDraw = 0;
    self.queue = [[Queue alloc] init];
    self.shapeFactory = [[ShapeFactory alloc] init];
    [self.shapeDrawingView.layer setCornerRadius:30.0f];
    
    // border
    [self.shapeDrawingView.layer setBorderColor:[UIColor lightGrayColor].CGColor];
    [self.shapeDrawingView.layer setBorderWidth:1.5f];
    
    // drop shadow
    [self.shapeDrawingView.layer setShadowColor:[UIColor blackColor].CGColor];
    [self.shapeDrawingView.layer setShadowOpacity:0.8];
    [self.shapeDrawingView.layer setShadowRadius:3.0];
    [self.shapeDrawingView.layer setShadowOffset:CGSizeMake(2.0, 2.0)];
}

- (void) setNumberOfShapesToRequest:(NSNumber *)numberOfShapes{
    self.numberOfShapes = numberOfShapes;
    int shapeCount = [self.numberOfShapes intValue];
    self.numberOfShapesLabel.text = [NSString stringWithFormat:NUMBER_OF_SHAPES_LBL,shapeCount];
}

- (IBAction)numberOfShapesChanged:(id)sender {
    [self setNumberOfShapesToRequest:[NSNumber numberWithInt:self.numberOfShapesSlider.value]];
}

-(void) drawShapeWith:(TetrisShape *) tetrisShape andAnimationDuration:(float) duration{
    NSLog(@"Creating Shape %d...",tetrisShape.shape.numberOfSides);
    [[NSOperationQueue mainQueue] addOperationWithBlock:^ {
    UIView *view =[self.shapeFactory createWithShape:tetrisShape.shape];
        if(view!=nil){
            view.alpha = 0.2;
            view.center = CGPointMake(self.shapeDrawingView.frame.size.width  / 2,
                                      view.frame.size.height );
            
                if(self.lastShapeView != nil){
                    [self.lastShapeView removeFromSuperview];
                }
                [self.shapeDrawingView addSubview:view];
                self.lastShapeView = view;
                float finalPosition = self.shapeDrawingView.frame.size.height - view.frame.size.height;
                [UIView animateWithDuration:duration
                                 animations:^{
                                     view.alpha = 1.0;
                                     view.center = CGPointMake(self.shapeDrawingView.frame.size.width  / 2,
                                                               finalPosition);
                                 }
                                 completion:^(BOOL finished){
                                     self.pendingShapesToBeDraw--;
                                 }];
        }
    }];
}

-(void) refreshShape {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @autoreleasepool {
            while(self.pendingShapesToBeDraw>0){
                NSLog(@"Refreshing Shape ...");
                if(!self.queue.isEmpty){
                    TetrisShape *tetrisShape = self.queue.dequeue;
                    [self drawShapeWith:tetrisShape andAnimationDuration:1.0];
                }
                [NSThread sleepForTimeInterval:3];
            }
            NSLog(@"All Shapes have been done...");
        }
    });
}

- (IBAction)onStartBtnClicked:(id)sender {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        @autoreleasepool {
            NSLog(@"dispatching gRPC call");
            [GRPCCall useInsecureConnectionsForHost:kHostAddress];
            [GRPCCall setUserAgentPrefix:@"ShapeService/1.0" forHost:kHostAddress];
            ShapeService *client = [[ShapeService alloc] initWithHost:kHostAddress];
            GetSomeTetrisShapes *request = [GetSomeTetrisShapes message];
            [[NSOperationQueue mainQueue] addOperationWithBlock:^ {
                self.requestButton.enabled = NO;
            }];
            request.startingIndex = 0;
            request.numberOfShapes = self.numberOfShapes.intValue;
            self.pendingShapesToBeDraw = request.numberOfShapes;
            [self refreshShape];
            request.intervalMs = 2;
            request.dropSpotsArray = [GPBUInt32Array arrayWithValue:50];
            [client getSomeTetrisShapesWithRequest:request eventHandler:^(BOOL done,TetrisShape *response,NSError *error) {
                if(!done){
                    NSLog(@"Shape type : %d", response.shape.numberOfSides);
                    TetrisShape *tetrisShape = response;
                    [self.queue enqueue:tetrisShape];
                }else{
                    [[NSOperationQueue mainQueue] addOperationWithBlock:^ {
                        self.requestButton.enabled = YES;
                    }];
                }
            }];
        }
    });
}

@end
