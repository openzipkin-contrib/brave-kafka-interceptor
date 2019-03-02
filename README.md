# Kafka Interceptor: Zipkin

Instrumentation to create traces from Kafka Clients (e.g. Consumer, Producer)
using Interceptors (see [here](https://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/producer/ProducerInterceptor.html)
and [here](https://kafka.apache.org/0100/javadoc/org/apache/kafka/clients/consumer/ConsumerInterceptor.html)).

This interceptors could be added to Kafka Connectors via configuration, and to other off-the-shelf components like 
Kafka REST Proxy, KSQL and so on.

## Installation

### Producer Interceptor

Producer Interceptor create spans on sending records. This span will only represent the time it took to 
execute the `on_send` method provided by the API, not how long to send the actual record, or any other latency.

#### Kafka Clients

Add Interceptor to Producer Configuration:

```java
    producerConfig.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, Collections.singletonList(TracingProducerInterceptor.class));
```
### Consumer Interceptor

Consumer Interceptor create spans on consumption of records. This span will only represent the time it took execute
the `on_consume` method provided by the API, not how long it took to commit, or any other latency. 

#### Kafka Clients

```java
    consumerConfig.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, Collections.singletonList(TracingConsumerInterceptor.class));
```

### Configuration

By default, Interceptors will use Kafka Cluster as transport for Zipkin spans, and will pick `client.id` as service name.

If you want to override these values, use this properties in your configuration:

| Key                           | Value                                                                                 |
|-------------------------------|---------------------------------------------------------------------------------------|
| `zipkin.api.url`              | URL where Zipkin API is deployed. If not present, `zipkin.bootstrap.servers` is used. |
| `zipkin.bootstrap.servers`    | Bootstrap Servers list to send Spans. if not present, `bootstrap.servers` (Kafka Client property) is used. |
| `zipkin.local.service_name`   | Application Service name used to tag span. If not present, and producer, `client.id` will be used. If not present and consumer, `group.id` is used. |
| `zipkin.remote.service_name`  | Remote Service to assign to Kafka. If not present, `"kafka"` is used.                 |
| `zipkin.sampler.rate`         | Rate to sample spans. Default: `1.0`                                                  |

### How to test it

Start Docker Compose [docker-compose.yml](blob/master/docker-compose.yml)

```
docker-compose up -d
```

In `src/test/io/github/jeqo/brave/kafka/interceptors/examples`, a Producer and Consumer is placed to test interceptors.

![](docs/trace.png)