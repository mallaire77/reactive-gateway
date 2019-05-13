//
//  Queue.m
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-05-01.
//  Copyright © 2019 Yoppworks. All rights reserved.
//
#import "Queue.h"

@interface Queue()

@property(nonatomic, strong) NSMutableArray *backingArray;

@end

@implementation Queue

-(id)init {
    self = [super init];
    
    if (self) {
        self.backingArray = [NSMutableArray array];
    }
    return self;
}

-(void)enqueue:(id<NSObject>)object {
    [self.backingArray addObject:object];
}

-(id)dequeue {
    id object = [self.backingArray lastObject];
    [self.backingArray removeObjectAtIndex:[self size]-1];
    return object;
}
-(BOOL)isEmpty{
    return self.backingArray.count == 0;
}
-(int) size{
    return (int)self.backingArray.count;
}
@end
