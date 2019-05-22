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

-(UIBezierPath *) createPolygonWithSides:(NSInteger) sides{
    double cornerRadius = 1.0;
    double lineWidth = 1.0;
    CGRect square = CGRectMake(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT);
    UIBezierPath * path = [[UIBezierPath alloc] init];
    double theta = 2.0 * M_PI / ((double)sides);
    double offset = cornerRadius * tan(theta / 2.0);
    double squarex = (double) square.size.width;
    double squarey = (double) square.size.height;
    double squareWidth = squarex>squarey?squarex:squarey;
    double length = squareWidth - lineWidth;
    if( sides % 4 != 0) {
        // ... offset it inside a circle inside the square
        length = length * cos(theta / 2.0) + offset/2.0;
    }
    double sideLength = length * tan(theta / 2.0);
    
    // start drawing at `point` in lower right corner
    CGFloat pointx = squareWidth / 2.0 + sideLength / 2.0 - offset;
    CGFloat pointy = squareWidth - (squareWidth - length) / 2.0;
    CGPoint point = CGPointMake(pointx,pointy);
    double angle = M_PI;
    [path moveToPoint:point];
    for (int i=0;i<sides;i++) {
        point = CGPointMake((double)point.x + (sideLength - offset * 2.0) * cos(angle),
                            (double)point.y + (sideLength - offset * 2.0) * sin(angle));
        [path addLineToPoint:point];
        CGPoint center = CGPointMake((double)point.x + cornerRadius * cos(angle + M_PI_2),
                                 (double)point.y + cornerRadius * sin(angle + M_PI_2));
        
        CGFloat startAngle = angle - M_PI_2;
        CGFloat endAngle = angle + theta - M_PI_2;
        [path addArcWithCenter:center radius:cornerRadius startAngle:startAngle endAngle:endAngle clockwise:true];
        point = path.currentPoint;
        angle += theta;
    }
    [path closePath];
    return path;
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
    float edgeRed = shape.edgeColor.red/255;
    float edgeGreen = shape.edgeColor.green/255;
    float edgeBlue = shape.edgeColor.blue/255;
    CGColorRef edgeColor = [[[UIColor alloc]initWithRed:edgeRed
                                                  green:edgeGreen
                                                   blue:edgeBlue
                                                  alpha:1.0] CGColor];
    float fillRed = shape.fillColor.red/255;
    float fillGreen = shape.fillColor.green/255;
    float fillBlue = shape.fillColor.blue/255;
    CGColorRef fillColor = [[[UIColor alloc]initWithRed:fillRed
                                                  green:fillGreen
                                                   blue:fillBlue
                                                  alpha:1.0] CGColor];
    UIBezierPath *polygonPath = [self createPolygonWithSides:shape.numberOfSides];
    return [self createUIViewWithPath:polygonPath andFill:fillColor andEdge:edgeColor];    
}
@end
