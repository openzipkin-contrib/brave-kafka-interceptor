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
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Record traces when records are sent to a Kafka Topic.
 * <p>
 * Extract context from incoming Record, if exist injected in its header, and use it to link it to
 * the Span created by the interceptor.
 */
public class TracingProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

  static final Logger LOGGER = LoggerFactory.getLogger(TracingProducerInterceptor.class);
  static final String SPAN_NAME = "on_send";

  TracingConfiguration configuration;
  Tracing tracing;
  String remoteServiceName;
  TraceContext.Injector<Headers> injector;
  TraceContext.Extractor<Headers> extractor;

  @Override public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {
    TraceContextOrSamplingFlags traceContextOrSamplingFlags = extractor.extract(record.headers());
    Span span = tracing.tracer().nextSpan(traceContextOrSamplingFlags);
    tracing.propagation().keys().forEach(key -> record.headers().remove(key));
    injector.inject(span.context(), record.headers());
    if (!span.isNoop()) {
      if (record.key() instanceof String && !"".equals(record.key())) {
        span.tag(KafkaInterceptorTagKey.KAFKA_KEY, record.key().toString());
      }
      span.tag(KafkaInterceptorTagKey.KAFKA_TOPIC, record.topic())
        .tag(KafkaInterceptorTagKey.KAFKA_CLIENT_ID,
          configuration.getString(ProducerConfig.CLIENT_ID_CONFIG))
        .name(SPAN_NAME).kind(Span.Kind.PRODUCER)
        .remoteServiceName(remoteServiceName).start();
    }
    span.finish();
    LOGGER.debug("Producer Record intercepted: {}", span.context());
    return record;
  }

  @Override public void onAcknowledgement(RecordMetadata recordMetadata, Exception exception) {
    // Do nothing
  }

  @Override public void close() {
    tracing.close();
  }

  @Override public void configure(Map<String, ?> configs) {
    configuration = new TracingConfiguration(configs);
    remoteServiceName =
      configuration.getStringOrDefault(TracingConfiguration.REMOTE_SERVICE_NAME_CONFIG, TracingConfiguration.REMOTE_SERVICE_NAME_DEFAULT);
    tracing = new TracingBuilder(configuration).build();
    extractor = tracing.propagation().extractor(KafkaInterceptorPropagation.HEADER_GETTER);
    injector = tracing.propagation().injector(KafkaInterceptorPropagation.HEADER_SETTER);
  }
}
