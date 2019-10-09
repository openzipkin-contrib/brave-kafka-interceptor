/*
 * Copyright 2018-2019 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package brave.kafka.interceptor;

import brave.Span;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static brave.kafka.interceptor.TracingConfiguration.REMOTE_SERVICE_NAME_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.REMOTE_SERVICE_NAME_DEFAULT;

/**
 * Record spans when records are received from Consumer API.
 * <p>
 * Creates a span per Record, and link it with an incoming context if stored in Records header.
 */
public class TracingConsumerInterceptor<K, V> implements ConsumerInterceptor<K, V> {

  static final Logger LOGGER = LoggerFactory.getLogger(TracingConsumerInterceptor.class);
  static final String SPAN_NAME = "on_consume";

  TracingConfiguration configuration;
  Tracing tracing;
  String remoteServiceName;
  TraceContext.Injector<Headers> injector;
  TraceContext.Extractor<Headers> extractor;

  @Override public ConsumerRecords<K, V> onConsume(ConsumerRecords<K, V> records) {
    if (records.isEmpty() || tracing.isNoop()) return records;
    Map<String, Span> consumerSpansForTopic = new LinkedHashMap<>();
    for (TopicPartition partition : records.partitions()) {
      String topic = partition.topic();
      List<ConsumerRecord<K, V>> recordsInPartition = records.records(partition);
      for (ConsumerRecord<K, V> record : recordsInPartition) {
        TraceContextOrSamplingFlags extracted = extractor
          .extract(record.headers());

        // If we extracted neither a trace context, nor request-scoped data
        // (extra),
        // make or reuse a span for this topic
        if (extracted.samplingFlags() != null && extracted.extra().isEmpty()) {
          Span consumerSpanForTopic = consumerSpansForTopic.get(topic);
          if (consumerSpanForTopic == null) {
            consumerSpansForTopic.put(topic,
              consumerSpanForTopic = tracing.tracer()
                .nextSpan(extracted).name("poll")
                .kind(Span.Kind.CONSUMER)
                .remoteServiceName(remoteServiceName)
                .tag(KafkaInterceptorTagKey.KAFKA_TOPIC, topic)
                .tag(KafkaInterceptorTagKey.KAFKA_GROUP_ID,
                  configuration.getString(
                    ConsumerConfig.GROUP_ID_CONFIG))
                .tag(KafkaInterceptorTagKey.KAFKA_CLIENT_ID,
                  configuration.getString(
                    ConsumerConfig.CLIENT_ID_CONFIG))
                .start());
          }
          // no need to remove propagation headers as we failed to extract
          // anything
          injector.inject(consumerSpanForTopic.context(), record.headers());
        } else { // we extracted request-scoped data, so cannot share a consumer
          // span.
          Span span = tracing.tracer().nextSpan(extracted);
          if (!span.isNoop()) {
            span.name(SPAN_NAME).kind(Span.Kind.CONSUMER)
              .remoteServiceName(remoteServiceName)
              .tag(KafkaInterceptorTagKey.KAFKA_TOPIC, topic)
              .tag(KafkaInterceptorTagKey.KAFKA_GROUP_ID,
                configuration.getString(
                  ConsumerConfig.GROUP_ID_CONFIG))
              .tag(KafkaInterceptorTagKey.KAFKA_CLIENT_ID, configuration
                .getString(ConsumerConfig.CLIENT_ID_CONFIG));
            span.start().finish(); // span won't be shared by other records
          }
          // remove prior propagation headers from the record
          tracing.propagation().keys()
            .forEach(key -> record.headers().remove(key));
          injector.inject(span.context(), record.headers());
        }
      }
    }
    consumerSpansForTopic.values().forEach(span -> {
      span.finish();
      LOGGER.debug("Consumer Record intercepted: {}", span.context());
    });
    return records;
  }

  @Override public void onCommit(Map<TopicPartition, OffsetAndMetadata> map) {
    // Do nothing
  }

  @Override public void close() {
    tracing.close();
  }

  @Override public void configure(Map<String, ?> configs) {
    configuration = new TracingConfiguration(configs);
    remoteServiceName =
      configuration.getStringOrDefault(REMOTE_SERVICE_NAME_CONFIG, REMOTE_SERVICE_NAME_DEFAULT);
    tracing = new TracingBuilder(configuration).build();
    extractor = tracing.propagation().extractor(KafkaInterceptorPropagation.HEADER_GETTER);
    injector = tracing.propagation().injector(KafkaInterceptorPropagation.HEADER_SETTER);
  }
}
