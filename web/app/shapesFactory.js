const shapesFactory = {};


shapesFactory.ShapesFactory = function(svgContainer) {
    this.container = svgContainer.append("g")
        .attr("transform", "translate(10, 10)");
};

shapesFactory.ShapesFactory.prototype.drawShape = function(shape, intervalMs) {
    var svgElement = this.buildShape(shape);
    this.addStyle(svgElement, shape);
    if(intervalMs > 1000) {
        this.addAnimation(svgElement);
    }
    
    return svgElement;
}
shapesFactory.ShapesFactory.prototype.buildShape = function(shape) {

    switch (shape.getNumberofsides()) {
        case 3:
          return this.buildTriangle(shape);
          break;
        case 4:
          return this.buildSquare(shape);
          break;
        case 5:
          return this.buildPentagon(shape);
          break;
        case 6:
          return this.buildHexagon(shape);
          break;
        case 7:
        return this.buildHeptagon(shape);
          break;
        case 8:
          return this.buildOctagon(shape);
          break;
        case 9:
          return this.buildSquare(shape);
          break;
        case 10:
          return this.buildSquare(shape);
          break;
    }
}

shapesFactory.ShapesFactory.prototype.buildTriangle = function(shape) {
    var poly = [
        {"x": 150, "y": 0},
        {"x": 0, "y": 300},
        {"x": 300, "y": 300}
    ];

    return this.buildPolygon(poly, shape.getHeight(), shape.getWidth());
}

shapesFactory.ShapesFactory.prototype.buildSquare = function(shape) {

    return this.container.append("rect")
        .attr("x", "0")
        .attr("y", "0")
        .attr("width", shape.getWidth())
        .attr("height", shape.getHeight());
};

shapesFactory.ShapesFactory.prototype.buildPentagon = function(shape) {
    var poly = [
        {"x": 150, "y": 0},
        {"x": 0, "y": 150},
        {"x": 0, "y": 300},
        {"x": 300, "y": 300},
        {"x": 300, "y": 150}
    ];

    return this.buildPolygon(poly, shape.getHeight(), shape.getWidth());
}

shapesFactory.ShapesFactory.prototype.buildHexagon = function(shape) {
    var poly = [
        {"x": 50, "y": 0}, 
        {"x": 0, "y": 150}, 
        {"x": 50, "y": 300}, 
        {"x": 250, "y": 300}, 
        {"x": 300, "y": 150}, 
        {"x": 250, "y": 0}
    ]

    return this.buildPolygon(poly, shape.getHeight(), shape.getWidth());
}

shapesFactory.ShapesFactory.prototype.buildHeptagon = function(shape) {
    var poly = [
        {"x": 150, "y": 0},
        {"x": 30, "y": 80},
        {"x": 0, "y": 200},
        {"x": 80, "y": 300},
        {"x": 220, "y": 300},
        {"x": 300, "y": 200},
        {"x": 270, "y": 80}
    ]

    return this.buildPolygon(poly, shape.getHeight(), shape.getWidth());
}

shapesFactory.ShapesFactory.prototype.buildOctagon = function(shape) {
    var poly = [
        {"x": 100, "y": 0},
        {"x": 0, "y": 100},
        {"x": 0, "y": 200},
        {"x": 100, "y": 300}, 
        {"x": 200, "y": 300},
        {"x": 300, "y": 200},
        {"x": 300, "y": 100},
        {"x": 200, "y": 0}
    ]

    return this.buildPolygon(poly, shape.getHeight(), shape.getWidth());
}

shapesFactory.ShapesFactory.prototype.buildPolygon = function(poly, height, width) {
    
    var x = d3.scaleLinear().range([0, width]);
	var y = d3.scaleLinear().range([0, height]);
  
    x.domain([0, 300]);
    y.domain([0, 300]);
    return this.container.selectAll("g")
        .data([poly])
        .enter().append("polygon")
        .attr("points", function(d){
        return d.map(function(d){
            return [x(d.x), y(d.y)].join(',')
        }).join(" ");      
        });
}

shapesFactory.ShapesFactory.prototype.addStyle = function(svgElement, shape) {
    var fill = shape.getFillcolor();
    var stroke = shape.getEdgecolor();

    return svgElement
    .style("fill", this.getRgb(fill))
    .style("stroke", this.getRgb(stroke))
    .style("stroke-width", "2");
}

shapesFactory.ShapesFactory.prototype.addAnimation = function(svgElement) {
   return svgElement.style("opacity", "0").transition().duration(1000).style("opacity", "1");
}

shapesFactory.ShapesFactory.prototype.clear = function() {
    this.container.selectAll("*").remove();
}

shapesFactory.ShapesFactory.prototype.getRgb = function(rgb) {
    return "rgb(" + rgb.getRed() + "," + rgb.getGreen() + "," + rgb.getBlue() + ")";
}

module.exports = shapesFactory;