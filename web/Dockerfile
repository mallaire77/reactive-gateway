FROM node:8-stretch

RUN apt-get -qq update && apt-get -qq install -y \
  unzip

RUN curl -sSL https://github.com/protocolbuffers/protobuf/releases/download/v3.7.1/\
protoc-3.7.1-linux-x86_64.zip -o /tmp/protoc.zip && \
  cd /tmp && \
  unzip -qq protoc.zip && \
  cp /tmp/bin/protoc /usr/local/bin/protoc

RUN curl -sSL https://github.com/grpc/grpc-web/releases/download/1.0.4/\
protoc-gen-grpc-web-1.0.4-linux-x86_64 -o /usr/local/bin/protoc-gen-grpc-web && \
  chmod +x /usr/local/bin/protoc-gen-grpc-web

RUN mkdir -p /var/www/html/dist
RUN mkdir -p /proto
RUN mkdir -p /app

COPY api/src/main/proto/reactive-gateway.proto /proto

ARG PROTO_DIR=/proto
ARG APP_DIR=/app

COPY web/app/*  $APP_DIR/

RUN protoc -I=$PROTO_DIR reactive-gateway.proto \
--js_out=import_style=commonjs:\
$APP_DIR \
--grpc-web_out=import_style=commonjs,mode=grpcwebtext:\
$APP_DIR


RUN cd $APP_DIR && \
  npm install && \
  npx webpack && \
  cp shapes.html /var/www/html && \
  cp dist/main.js /var/www/html/dist

EXPOSE 8082
WORKDIR /var/www/html
CMD ["python", "-m", "SimpleHTTPServer", "8082"]

