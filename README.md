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

Start Docker Compose [docker-compose.yml](docker-compose.yml)

```bash
docker-compose up -d
```

Steps to test:
* Navigate to http://localhost:8080 and login using `postgres` as server, `postgres` as username and `example` as password

* Create a table `source_table` with an auto-increment `id` and `name` field

* Once table is created deploy source and sink connectors using Makefile:

```bash
make docker-kafka-connectors
```

> Retry if connector workers are not ready yet, or table is not yet created.

* Insert values on table `source_table` to the table and check the traces in `http://localhost:9411`.

* Check `sink` topic has data with `kafkacat` or other CLI tool:

```bash
$ kafkacat -b localhost:29092 -C -t jdbc_source_table
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":false,"field":"name"}],"optional":false,"name":"source_table"},"payload":{"id":1,"name":"asdf"}}
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":false,"field":"name"}],"optional":false,"name":"source_table"},"payload":{"id":1,"name":"asdfa"}}
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":false,"field":"name"}],"optional":false,"name":"source_table"},"payload":{"id":2,"name":"agdsg"}}
% Reached end of topic jdbc_source_table [0] at offset 3
```

* To add KSQL spans, create a `stream` in KSQL to add its spans to existing traces:

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
