[![Gitter chat](http://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg)](https://gitter.im/openzipkin/zipkin)
[![Build Status](https://www.travis-ci.org/openzipkin-contrib/brave-kafka-interceptor.svg?branch=master)](https://www.travis-ci.org/openzipkin-contrib/brave-kafka-interceptor)

# Kafka Interceptor: Zipkin

Kafka [Consumer](https://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerInterceptor.html)
and
[Producer](https://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/ProducerInterceptor.html)
Interceptor to record tracing data.

This interceptors could be added to Kafka Connectors via configuration and to other off-the-shelf
components like Kafka REST Proxy, KSQL and so on.

## Installation

### Producer Interceptor

Producer Interceptor create spans on sending records. This span will only represent the time it took to
execute the `on_send` method provided by the API, not how long to send the actual record, or any other latency.

#### Kafka Clients

Add Interceptor to Producer Configuration:

```java
    producerConfig.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, Collections.singletonList(TracingProducerInterceptor.class));
    producerConfig.put("interceptor.classes", "brave.kafka.interceptor.TracingProducerInterceptor");
```
### Consumer Interceptor

Consumer Interceptor create spans on consumption of records. This span will only represent the time it took execute
the `on_consume` method provided by the API, not how long it took to commit, or any other latency.

#### Kafka Clients

```java
    consumerConfig.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, Collections.singletonList(TracingConsumerInterceptor.class));
    consumerConfig.put("interceptor.classes", "brave.kafka.interceptor.TracingConsumerInterceptor");
```

### Configuration

| Key | Value |
|-----|-------|
| `zipkin.sender.type` | Sender type: `NONE`(default), `KAFKA`, `HTTP` |
| `zipkin.encoding` | Zipkin encoding: `JSON`(default), `PROTO3`. |
| `zipkin.http.endpoint` | Zipkin HTTP Endpoint sender. |
| `zipkin.kafka.bootstrap.servers` | Bootstrap Servers list to send Spans. if not present, `bootstrap.servers` (Kafka Client property) is used. |
| `zipkin.local.service.name` | Application Service name used to tag span. Default: kafka-client. |
| `zipkin.trace.id.128bit.enabled` | Trace ID 128 bit enabled, default: `true` |
| `zipkin.sampler.rate` | Rate to sample spans. Default: `1.0` |

### How to test it

Before starting components, make sure to build the project to have JAR files available for containers:

```bash
make build
# or
./mvnw clean package
```

Start Docker Compose [docker-compose.yml](docker-compose.yml)

```bash
docker-compose up -d
```

Steps to test:
* Create a table `source_table`:

```bash
./create-table.sh
```

* Once table is created deploy source and sink connectors using Makefile:

```bash
make docker-kafka-connectors
```

* Insert values to the table:

```bash
./create-data.sh
```
* Check the traces.

* Create a Stream in KSQL:

```bash
$CONFLUENT_HOME/bin/ksql http://localhost:8088
#...
ksql> CREATE STREAM source_stream (id BIGINT, name VARCHAR) WITH (KAFKA_TOPIC='jdbc_source_table', VALUE_FORMAT='JSON');
ksql> SELECT id, name FROM source_stream;
```

* Finally, traces should look like this:

Search:

![](docs/search.png)

Trace view:

![](docs/trace.png)

Dependencies:

![](docs/dependencies.png)
