//
//  Queue.h
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-05-01.
//  Copyright © 2019 Yoppworks. All rights reserved.
//

#ifndef Queue_h
#define Queue_h
#import <Foundation/Foundation.h>

@interface Queue : NSObject

-(void)enqueue:(id)object;
-(id)dequeue;
-(BOOL)isEmpty;
-(int)size;

@end

#endif /* Queue_h */
