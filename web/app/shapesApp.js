const {ShapesFactory} = require('./shapesFactory');

const shapesApp = {};

/**
 * @param {Object} service
 * @param {Object} ctors
 * @param {Object} handlers
 */
shapesApp.ShapesApp = function(service, ctors, handlers) {
  this.service = service;
  this.ctors = ctors;
  this.handlers = handlers;
  this.shapesFactory =  {};
  this.uniqId = Math.round(new Date().getTime() + (Math.random() * 100));
};


shapesApp.ShapesApp.prototype.getSomeShapes = function(intervalMs, numberOfShapes) {
  var self = this;
  var streamRequest = new this.ctors.GetSomeShapes();
  streamRequest.setNumberofshapes(numberOfShapes);
  streamRequest.setIntervalMs(intervalMs);

  var stream = this.service.getSomeShapes(
    streamRequest,
    {
      'grpc-accept-encoding': 'identity',
      'content-accept-encoding': 'identity',
      'X-USERNAME': self.uniqId
    });

  stream.on('data', function(response) {

    if(response.getViable()) {
      var shape = response.getShape();
      self.shapesFactory.clear();
      self.shapesFactory.drawShape(shape, intervalMs);
    } else {
      self.toggleModal(response.getError());
    }

  });

  stream.on('error', function(err) {
    console.log('error', err)
  });

  stream.on('end', function() {
    console.log("stream end signal received");
  });

};

shapesApp.ShapesApp.prototype.releaseShapes = function() {
  var releaseShapesRequest = new this.ctors.ReleaseShapes();
  var self = this;
  this.service.releaseShapes(
    releaseShapesRequest,
    {
      'grpc-accept-encoding': 'identity',
      'content-accept-encoding': 'identity',
      'X-USERNAME': self.uniqId
    },
    function(err, response) {
      if (err) {
        console.log('error', err);
        self.toggleModal('Oops ! Something went wrong.');
      } else {
        var msg;
        if(response.getViable()) {
          msg = "Shapes released"
        } else {
          msg = response.getError();
        }
        self.toggleModal(msg)
      }
  });
};

shapesApp.ShapesApp.prototype.toggleModal = function(msg) {
  $('#modalMsg').text(msg);
  $('#msgModal').modal('toggle');
};


/**
 * @param {string} numberOfShapes
 */
shapesApp.ShapesApp.prototype.prepareShapes = function(numberOfShapes) {
  var self = this;
  var unaryRequest = new this.ctors.PrepareShapes();
  unaryRequest.setNumberofshapestoprepare(numberOfShapes);

  this.service.prepareShapes(
    unaryRequest,
    {
    'grpc-accept-encoding': 'identity',
    'content-accept-encoding': 'identity',
    'X-USERNAME': self.uniqId
    },
    function(err, response) {
      if (err) {
        console.log('error', err)
        self.toggleModal('Oops ! Something went wrong.');
      }else {
        if(response.getViable()) {
          self.toggleModal('You can stream shapes now !')
        } else {
          self.toggleModal(response.getError());
        }
      }
  });
};

shapesApp.ShapesApp.prototype.stream = function(e) {
  var intervalMs = $("#intervalMs").val();
  var numberOfShapes = $("#numberOfShapes").val();
  $("#intervalMs").val('');
  $("#numberOfShapes").val('');

  this.getSomeShapes(intervalMs, numberOfShapes);
}

shapesApp.ShapesApp.prototype.send = function(e) {
  var numberOfShapes = $("#inputNumberOfShapes").val();
  $("#inputNumberOfShapes").val('');
  if (!numberOfShapes) {
    this.toggleModal('Number of shapes required')
    return false;
  }

  this.prepareShapes(numberOfShapes);

  return false;
};

/**
 * Load the shapes app
 */
shapesApp.ShapesApp.prototype.load = function() {
  var self = this;
  $(document).ready(function() {
    // event handlers
    $("#send").click(self.send.bind(self));

    $("#stream").click(self.stream.bind(self));

    $("#inputNumberOfShapes").focus();

    self.shapesFactory = new ShapesFactory(
      d3.select('#streamContainer').append("svg").attr("width", 300).attr("height", 260)
    );

    $("#releaseShapes").click(self.releaseShapes.bind(self));

  });

};

module.exports = shapesApp;
