# Reactive Gateway Service Demo with gRPC
## Introduction
This project uses gRPC to demonstrate a reactive gateway service for the
2019 Reactive Horizons conference (Yoppworks hosted). 

## What Is A Gateway Service?
A gateway service is any service that provides an external (public) interface
to an internal set of microservices. It's responsibilities are to:
* authenticate and authorize users
* keep track of client state (e.g. Saga pattern)
* translate the external protocol (e.g. HTTPS) into the internal protocol (e
.g. Kafka topics)
* coordinate external requests with internal responses

## What is a Reactive Gateway Service?
Making a gateway service reactive means upholding the principles set forth in 
the [Reactive Manifesto](http://reactivemanifesto.org), that is:
* Responsive
* Elastic
* Resilient
* Message Driven

## Demonstration
There are three sub-projects in this code repository that implement the separate
portions of this demonstration:
* Server - a simple Akka-HTTP based server that services random shapes
* Mobile - an iPhone application that shows the shapes
* Web - a web application that shows the shapes
The fourth sub-project, api, is simply a definition of the gRPC service the 
other components use. 

# Build Instructions

# Demo Instructions

# Design
