Antiope
=======

This project provides a framework for implementing API Clients in Java.
For more information on the genesis and foundation of this project, see blog post [here](http://mezzetin.blogspot.com/2014/05/anatomy-of-api-client.html).

Antiope-Core
------------

First framework heavily based on the core of the [AWS Java SDK](https://github.com/aws/aws-sdk-java).
Typically your client will extend either [BaseAPIClient](https://github.com/lpezet/antiope/blob/master/antiope-core/src/main/java/com/github/lpezet/antiope/bo/BaseAPIClient.java) or [AdvancedAPIClient](https://github.com/lpezet/antiope/blob/master/antiope-core/src/main/java/com/github/lpezet/antiope/bo/AdvancedAPIClient.java) and expose public methods for each service you provide. 

For some samples, see [antiope-samples](#Antiope-Sample).

Antiope-AWS
-----------

This project provides a CloudWatch implementation of [IMetricsCollector](https://github.com/lpezet/antiope/blob/master/antiope-core/src/main/java/com/github/lpezet/antiope/metrics/IMetricsCollector.java).
When collected, metrics (even custom ones) will be send and tracked by [AWS CloudWatch](http://aws.amazon.com/cloudwatch/).

Antiope-Samples
---------------

This project provides a Yahoo RSS Weather Client providing implementations for both [BaseAPIClient](https://github.com/lpezet/antiope/blob/master/antiope-core/src/main/java/com/github/lpezet/antiope/bo/BaseAPIClient.java) and [AdvancedAPIClient](https://github.com/lpezet/antiope/blob/master/antiope-core/src/main/java/com/github/lpezet/antiope/bo/AdvancedAPIClient.java).


Antiope2-Core
-------------

Complete overhaul of **antiope-core** using [Worker Pattern](http://mezzetin.blogspot.com/2014/04/worker-pattern.html) to provide high level of customizations.

