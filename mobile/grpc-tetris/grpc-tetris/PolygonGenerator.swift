//
//  PolygonGenerator.swift
//  grpc-tetris
//
//  Created by Jorge Gonzalez on 2019-05-22.
//  Copyright © 2019 Yoppworks. All rights reserved.
//

import Foundation
import UIKit

public class PolygonGenerator : NSObject{
    
    
    public func generate(
        square:CGRect,
        lineWidth:Double,
        sides:NSInteger,
        cornerRadius:Double) -> UIBezierPath {
        
        let path = UIBezierPath()
        
        // how much to turn at every corner
        let theta = 2.0 * Double.pi / Double(sides)
        // offset from which to start rounding corners
        let offset = cornerRadius * tan(theta / 2.0)
        let squareWidth = min(Double(square.size.width), Double(square.size.height))
        // calculate the length of the sides of the polygon
        var length = squareWidth - lineWidth
        
        // if not dealing with polygon which will be square with all sides...
        if sides % 4 != 0 {
            // ... offset it inside a circle inside the square
            length = length * cos(theta / 2.0) + offset/2.0
        }
        let sideLength = length * tan(theta / 2.0)
        
        // start drawing at `point` in lower right corner
        
        var point = CGPoint(x: CGFloat(squareWidth / 2.0 + sideLength / 2.0 - offset),
                            y: CGFloat(squareWidth - (squareWidth - length) / 2.0))
        var angle = Double.pi
        path.move(to: point)
        
        // draw the sides and rounded corners of the polygon
        let pi2 = Double.pi/2
        for side in 1...sides {
            point = CGPoint(x: CGFloat(Double(point.x) + (sideLength - offset * 2.0) * cos(angle)),
                            y: CGFloat(Double(point.y) + (sideLength - offset * 2.0) * sin(angle)))
            path.addLine(to: point)
            let center = CGPoint(x: CGFloat(Double(point.x) + cornerRadius * cos(angle + pi2)),
                                 y: CGFloat(Double(point.y) + cornerRadius * sin(angle + pi2)))
            
            path.addArc(withCenter: center, radius: CGFloat(cornerRadius), startAngle: CGFloat(angle - pi2), endAngle: CGFloat(angle + theta - pi2), clockwise: true)
            
            point = path.currentPoint // we don't have to calculate where the arc ended ... UIBezierPath did that for us
            
            angle += theta
        }
        path.close()
        
        return path
    }
}
