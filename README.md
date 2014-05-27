# Mule Metrics

[![Build Status](https://snap-ci.com/elZXlVG6g4wDiDucm5MaFtyRqxDMg1wAdVVkW0ZoH60/build_image)](https://snap-ci.com/projects/GeoNet/mule-metrics/build_history)

Mule application to provide metrics for the JVM running the Mule server and applications running in it.

* Requires Java 7+ (for the Jetty http client).
* Tested on Mule 3.4


## Quick Start

### Send Metrics to the Mule Log Files

With no additional configuration this application sends metrics values to the Mule logs every twenty seconds.

* Download a release of this application (https://github.com/GeoNet/mule-metrics/releases) and deploy it to your Mule server.
* Once the application starts then tail the logs to see metrics values for the Mule JVM and all Mule applications running in
it (including this one).


### Send Metrics to Hosted Graphite

Send metrics to Hosted Graphite (https://www.hostedgraphite.com) every twenty seconds.  See the dashboard images under `screen-shots`.

* Create a Hosted Graphite account https://www.hostedgraphite.com
* Create an api access key for the account.
* Copy the properties file from this project `src/main/resources/mule-metrics.properties` to the /etc/mule.
* Edit `/etc/mule/mule-metrics.properties`, uncomment and set the `hostedgraphite.api.key=XXX` to the one for your account.
* Download a release of this application (https://github.com/GeoNet/mule-metrics/releases) and deploy it to your Mule server.
* Log into Hosted Graphite and create dashboards (it can take a few minutes for metrics to appear in Hosted Grpahite for the first time).


### Send Metrics to Librato Metrics

Send metrics to Librato Metrics (https://metrics.librato.com/) every twenty seconds.

* Create a Librato Metrics account https://metrics.librato.com/
* Create a key with Record Access for the Librato Metrics account.
* Copy the properties file from this project `src/main/resources/mule-metrics.properties` to the conf dir in your Mule installation e.g., `/etc/mule`
* Edit `/etc/mule/mule-metrics.properties`, uncomment and set Librato user and api key values to the ones you just created.

```
  librato.user=some.user@mail.com
  librato.api.key=XXX
```

* Download a release of this application (https://github.com/GeoNet/mule-metrics/releases) and deploy it to your Mule server.

* Log into Librato Metrics and create dashboards.  The metrics are sent with the host name as the source.
 This makes them very suitable for using with dynamic instruments and dashboards.


### Other Metrics Targets

A Sender is used to get the metrics to the storage and visualisation end point.  You only need to implement one method and add to the SenderFactory e.g.,

 * implementing `nz.org.geonet.mule.metrics.sender.Sender`
 * and creating that Sender from `nz.org.geonet.mule.metrics.sender.SenderFactory` when appropriate configuration is available.

## Metrics

The following metrics are collected:

### Mule JVM

For the JVM running Mule:

* Mule.ProcessCpuLoad
* Mule.SystemCpuLoad
* Mule.Threading.max
* Mule.Threading.used
* Mule.Classes.totalLoaded
* Mule.HeapMemoryUsage.max
* Mule.HeapMemoryUsage.used

### Mule Processing:

These values are totals for Mule server for the collection interval.

* Mule.AsyncEventsReceived
* Mule.SyncEventsReceived
* Mule.TotalEventsReceived
* Mule.ProcessedEvents
* Mule.EventProcessingTime
* Mule.ExecutionErrors
* Mule.FatalErrors

### Mule Application Processing:

These values are per Mule application values for the collection interval e.g., for this application (mule-metrics):

* Mule.mule-metrics.AsyncEventsReceived
* Mule.mule-metrics.EventProcessingTime
* Mule.mule-metrics.ExecutionErrors
* Mule.mule-metrics.FatalErrors
* Mule.mule-metrics.ProcessedEvents
* Mule.mule-metrics.SyncEventsReceived
* Mule.mule-metrics.TotalEventsReceived


## General Options

### Properties file

The following properties can also be set in `/etc/mule/mule-metrics.properties`.

* jolokia.port=8899 - the port that Jolokia communicates on (see also Security below).
* collection.interval.millis=20000 - the interval that metrics are collected at.
* perAppMetrics=true - set true to collect metrics for each application running in Mule, false to disable.  Totals for
 the Mule instance will be included in either case.

## Security

 Jolokia is used for the HTTP-JMX bridge.  Access to the embedded Jolokia servlet is restricted to 127.0.0.1 only by the
 file `src/main/resources/jolokia-access.xml`.  This can be further restricted by firewalling access to the Jolokia port.

 See also http://www.jolokia.org/reference/html/security.html

 Jolokia could be used to further control the Mule instance and applications via the HTTP-JMX bridge.


## Under the Covers.

* Jolokia (http://www.jolokia.org/) is used for the HTTP-JMX bridge and client.  Jolokia is awesome!
* The Jetty HTTP Client (http://www.eclipse.org/jetty/documentation/current/http-client-api.html) is used for sending metrics.  HTTP with Java
  made simple and powerful!



