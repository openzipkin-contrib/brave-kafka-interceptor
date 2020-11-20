/*
 * Copyright 2018-2020 The OpenZipkin Authors
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

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TracingProducerInterceptorTest extends BaseTracingTest {

  ProducerRecord<String, String> record = new ProducerRecord<>("topic", "value");

  @Test void shouldNotTouchRecords() {
    TracingProducerInterceptor<String, String> interceptor = new TracingProducerInterceptor<>();
    interceptor.configure(map);
    ProducerRecord<String, String> tracedRecord = interceptor.onSend(record);
    assertThat(tracedRecord).isEqualTo(record);
  }

  @Test void shouldCreateSpanOnSend() {
    // Given
    TracingProducerInterceptor<String, String> interceptor = new TracingProducerInterceptor<>();
    interceptor.configure(map);
    interceptor.tracing = tracing;
    // When
    interceptor.onSend(record);
    // Then
    assertThat(spans).isNotEmpty();
  }

  @Test void shouldCreateChildSpanIfContextAvailable() {
    // Given
    TracingProducerInterceptor<String, String> interceptor = new TracingProducerInterceptor<>();
    interceptor.configure(map);
    interceptor.tracing = tracing;
    brave.Span span = tracing.tracer().newTrace();
    tracing.propagation().injector(KafkaInterceptorPropagation.HEADER_SETTER)
      .inject(span.context(), record.headers());
    // When
    interceptor.onSend(record);
    // Then
    assertThat(spans).hasSize(1);
    assertThat(spans.get(0).parentId()).isEqualTo(span.context().spanIdString());
  }
}
