//
//  ShapeFactory.m
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-05-01.
//  Copyright © 2019 Yoppworks. All rights reserved.
//

#import "ShapeFactory.h"

@implementation ShapeFactory
- (UIView *) createCircle{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0,0, 100, 100)];
    UIBezierPath* ovalPath = [UIBezierPath bezierPathWithOvalInRect: CGRectMake(0,0,view.frame.size.width,view.frame.size.height)];
    [UIColor.grayColor setFill];
    [ovalPath fill];
    CAShapeLayer *circleMaskLayer = [CAShapeLayer layer];
    [circleMaskLayer setPath:ovalPath.CGPath];
    
    view.backgroundColor = [UIColor colorWithWhite:.75 alpha:1];
    view.layer.mask = circleMaskLayer;
    return view;
}

- (UIView *) createLine{
    return nil;
}

- (UIView *) createTriangle{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0,0, 100, 100)];
    UIBezierPath* trianglePath = [UIBezierPath bezierPath];
    
    [trianglePath moveToPoint:CGPointMake(0, view.frame.size.height)];
    [trianglePath addLineToPoint:CGPointMake(view.frame.size.width, view.frame.size.height)];
    [trianglePath addLineToPoint:CGPointMake(view.frame.size.width/2, 0)];
    [trianglePath closePath];
    
    CAShapeLayer *triangleMaskLayer = [CAShapeLayer layer];
    [triangleMaskLayer setPath:trianglePath.CGPath];
    
    view.backgroundColor = [UIColor colorWithWhite:.75 alpha:1];
    view.layer.mask = triangleMaskLayer;
    return view;
}

- (UIView *) createSquare{
    UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0,0, 100, 100)];
    UIBezierPath* rectanglePath = [UIBezierPath bezierPathWithRect: CGRectMake(0,0,view.frame.size.width,view.frame.size.height)];
    [UIColor.grayColor setFill];
    [rectanglePath fill];
    CAShapeLayer *circleMaskLayer = [CAShapeLayer layer];
    [circleMaskLayer setPath:rectanglePath.CGPath];
    
    view.backgroundColor = [UIColor colorWithWhite:.75 alpha:1];
    view.layer.mask = circleMaskLayer;
    return view;
}

- (UIView *) createWithType:(ShapeType) shapeType{
    switch (shapeType) {
        case CIRCLE:
            return [self createCircle];
        case LINE:
            return [self createTriangle];
        case TRIANGLE:
            return [self createTriangle];
        case SQUARE:
            return [self createSquare];
        default:
            break;
    }
    return nil;
}
@end
