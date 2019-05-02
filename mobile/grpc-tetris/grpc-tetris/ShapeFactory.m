//
//  ShapeFactory.m
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-05-01.
//  Copyright © 2019 Yoppworks. All rights reserved.
//

#import "ShapeFactory.h"
const int DEFAULT_WIDTH = 100;
const int DEFAULT_HEIGHT = 100;

@implementation ShapeFactory

-(UIView *) createUIViewWithPath:(UIBezierPath *) path andFill:(CGColorRef) fillColor andEdge:(CGColorRef) edgeColor{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0,0, DEFAULT_WIDTH, DEFAULT_HEIGHT)];
    [path closePath];CAShapeLayer *layer = [CAShapeLayer layer];
    [layer setPath:path.CGPath];
    layer.strokeColor = edgeColor;
    layer.fillColor = fillColor;
    [view.layer addSublayer:layer];
    return view;
}

- (UIView *) createCircleWithFill:(CGColorRef) fillColor andEdge:(CGColorRef) edgeColor{
    UIBezierPath* ovalPath = [UIBezierPath bezierPathWithOvalInRect: CGRectMake(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT)];
    return [self createUIViewWithPath:ovalPath andFill:fillColor andEdge:edgeColor];
}

- (UIView *) createLine{
    return nil;
}

- (UIView *) createTriangleWithFill:(CGColorRef) fillColor andEdge:(CGColorRef) edgeColor{
    UIBezierPath* trianglePath = [UIBezierPath bezierPath];
    [trianglePath moveToPoint:CGPointMake(0, DEFAULT_HEIGHT)];
    [trianglePath addLineToPoint:CGPointMake(DEFAULT_WIDTH, DEFAULT_HEIGHT)];
    [trianglePath addLineToPoint:CGPointMake(DEFAULT_WIDTH/2, 0)];
    return [self createUIViewWithPath:trianglePath andFill:fillColor andEdge:edgeColor];
}

- (UIView *) createSquareWithFill:(CGColorRef) fillColor andEdge:(CGColorRef) edgeColor{
    UIBezierPath* rectanglePath = [UIBezierPath bezierPathWithRect: CGRectMake(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT)];
    return [self createUIViewWithPath:rectanglePath andFill:fillColor andEdge:edgeColor];
}

- (UIView *) createWithShape:(Shape *) shape{
    ShapeType shapeType = shape.numberOfSides;
    float edgeRed = shape.edgeColor.red;
    float edgeGreen = shape.edgeColor.green;
    float edgeBlue = shape.edgeColor.blue;
    CGColorRef edgeColor = [[[UIColor alloc]initWithRed:edgeRed
                                                  green:edgeGreen
                                                   blue:edgeBlue
                                                  alpha:1.0] CGColor];
    float fillRed = shape.fillColor.red;
    float fillGreen = shape.fillColor.green;
    float fillBlue = shape.fillColor.blue;
    CGColorRef fillColor = [[[UIColor alloc]initWithRed:fillRed
                                                  green:fillGreen
                                                   blue:fillBlue
                                                  alpha:1.0] CGColor];
    
    switch (shapeType) {
        case CIRCLE:
            return [self createCircleWithFill:fillColor andEdge:edgeColor];
        case LINE:
            return [self createTriangleWithFill:fillColor andEdge:edgeColor];
        case TRIANGLE:
            return [self createTriangleWithFill:fillColor andEdge:edgeColor];
        case SQUARE:
            return [self createSquareWithFill:fillColor andEdge:edgeColor];
        default:
            break;
    }
    return nil;
}
@end
