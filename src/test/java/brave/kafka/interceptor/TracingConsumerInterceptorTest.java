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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TracingConsumerInterceptorTest extends BaseTracingTest {

  @Test void shouldNotTouchRecords() {
    // Given
    Map<TopicPartition, List<ConsumerRecord<String, String>>> topicPartitionAndRecords =
      new HashMap<>();
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L,
      "k", "v");
    topicPartitionAndRecords.put(new TopicPartition("topic", 0),
      Collections.singletonList(record));
    ConsumerRecords<String, String> records = new ConsumerRecords<>(
      topicPartitionAndRecords);
    TracingConsumerInterceptor<String, String> interceptor =
      new TracingConsumerInterceptor<>();
    interceptor.configure(map);
    // When
    ConsumerRecords tracedRecords = interceptor.onConsume(records);
    // Then
    assertThat(tracedRecords).isEqualTo(records);
  }

  @Test void shouldCreateSpansOnConsume() {
    // Given
    Map<TopicPartition, List<ConsumerRecord<String, String>>> topicPartitionAndRecords =
      new HashMap<>();
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L,
      "k", "v");
    topicPartitionAndRecords.put(new TopicPartition("topic", 0),
      Arrays.asList(record, record, record));
    ConsumerRecords<String, String> records = new ConsumerRecords<>(
      topicPartitionAndRecords);
    TracingConsumerInterceptor<String, String> interceptor =
      new TracingConsumerInterceptor<>();
    interceptor.configure(map);
    interceptor.tracing = tracing;
    // When
    interceptor.onConsume(records);
    // Then
    assertThat(spans).hasSize(3);
  }
}
