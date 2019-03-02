/*
 * Copyright 2018-2019 Sysco Middleware
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
package no.sysco.middleware.kafka.interceptor.zipkin;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import zipkin2.Span;

import java.util.*;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;

public class TracingConsumerInterceptorTest extends BaseTracingTest {

	@Test
	public void shouldNotTouchRecords() {
		// Given
		final Map<TopicPartition, List<ConsumerRecord<String, String>>> topicPartitionAndRecords = new HashMap<>();
		final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L,
				"k", "v");
		topicPartitionAndRecords.put(new TopicPartition("topic", 0),
				Collections.singletonList(record));
		final ConsumerRecords<String, String> records = new ConsumerRecords<>(
				topicPartitionAndRecords);
		final TracingConsumerInterceptor<String, String> interceptor = new TracingConsumerInterceptor<>();
		interceptor.configure(map);
		// When
		final ConsumerRecords tracedRecords = interceptor.onConsume(records);
		// Then
		assertEquals(records, tracedRecords);
	}

	@Test
	public void shouldCreateSpansOnConsume() {
		// Given
		final Map<TopicPartition, List<ConsumerRecord<String, String>>> topicPartitionAndRecords = new HashMap<>();
		final ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L,
				"k", "v");
		topicPartitionAndRecords.put(new TopicPartition("topic", 0),
				Arrays.asList(record, record, record));
		final ConsumerRecords<String, String> records = new ConsumerRecords<>(
				topicPartitionAndRecords);
		final TracingConsumerInterceptor<String, String> interceptor = new TracingConsumerInterceptor<>();
		interceptor.configure(map);
		interceptor.tracing = tracing;
		// When
		interceptor.onConsume(records);
		final Spliterator<Span> span = spans.spliterator();
		long count = StreamSupport.stream(span, false).count();
		assertEquals(3, count);
	}

}
