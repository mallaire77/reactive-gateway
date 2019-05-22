//
//  ShapeFactory.h
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-05-01.
//  Copyright © 2019 Yoppworks. All rights reserved.
//

#ifndef ShapeFactory_h
#define ShapeFactory_h
#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import <grpc-tetris/ReactiveGateway.pbrpc.h>
#import <grpc_tetris-Swift.h>
typedef enum shapeType
{
    CIRCLE = 0,
    LINE = 1,
    TRIANGLE = 2,
    SQUARE = 3,
} ShapeType;

@interface ShapeFactory: NSObject
- (UIView *) createWithShape:(Shape *) shape;
@end
#endif /* ShapeFactory_h */
