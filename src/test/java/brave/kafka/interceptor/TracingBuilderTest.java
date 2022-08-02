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

import brave.sampler.Sampler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.junit.jupiter.api.Test;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka.KafkaSender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import static brave.kafka.interceptor.TracingConfiguration.ENCODING_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.ENCODING_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.KAFKA_BOOTSTRAP_SERVERS_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.SAMPLER_RATE_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.SAMPLER_RATE_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.SENDER_TYPE_CONFIG;
import static org.assertj.core.api.Assertions.assertThat;

class TracingBuilderTest {

  @Test void shouldBuildDefaultEncoding() {
    // Given
    Map<String, String> map = new HashMap<>();
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Encoding encoding = new TracingBuilder.EncodingBuilder(config).build();
    // Then
    assertThat(encoding).isEqualTo(Encoding.valueOf(ENCODING_DEFAULT));
  }

  @Test void shouldBuildEncoding() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(ENCODING_CONFIG, Encoding.PROTO3.name());
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Encoding encoding = new TracingBuilder.EncodingBuilder(config).build();
    // Then
    assertThat(encoding).isEqualTo(Encoding.PROTO3);
  }

  @Test void shouldBuildDefaultSampler() {
    // Given
    Map<String, String> map = new HashMap<>();
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sampler sampler = new TracingBuilder.SamplerBuilder(config).build();
    // Then
    float defaultSampler = Float.parseFloat(SAMPLER_RATE_DEFAULT);
    assertThat(sampler).isEqualTo(Sampler.create(defaultSampler));
  }

  @Test void shouldBuildSampler() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SAMPLER_RATE_CONFIG, "0.5");
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sampler sampler = new TracingBuilder.SamplerBuilder(config).build();
    // Then
    assertThat(sampler).isNotNull();
  }

  @Test void shouldBuildSamplerWithFallback() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SAMPLER_RATE_CONFIG, "1.5");
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sampler sampler = new TracingBuilder.SamplerBuilder(config).build();
    // Then
    assertThat(sampler).isNotNull();
  }

  @Test void shouldBuildNullSender() {
    // Given
    Map<String, String> map = new HashMap<>();
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isNull();
  }

  @Test void shouldBuildNoneSender() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SENDER_TYPE_CONFIG, TracingBuilder.SenderBuilder.SenderType.NONE.name());
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isNull();
  }

  @Test void shouldBuildHttpSender() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SENDER_TYPE_CONFIG, TracingBuilder.SenderBuilder.SenderType.HTTP.name());
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isInstanceOf(OkHttpSender.class);
  }

  @Test void shouldBuildKafkaSenderWithConfig() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SENDER_TYPE_CONFIG, TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
    map.put(KAFKA_BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isInstanceOf(KafkaSender.class);
  }

  @Test void shouldBuildKafkaSenderWithDefault() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SENDER_TYPE_CONFIG, TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
    map.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isInstanceOf(KafkaSender.class);
  }

  @Test void shouldBuildKafkaSenderWithList() {
    // Given
    Map<String, Object> map = new HashMap<>();
    map.put(SENDER_TYPE_CONFIG, TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
    map.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
      Arrays.asList("localhost:9092", "localhost:9094"));
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isInstanceOf(KafkaSender.class);
  }


  @Test void shouldBuildKafkaSenderWithOverrides() {
    // Given
    Map<String, String> map = new HashMap<>();
    map.put(SENDER_TYPE_CONFIG, TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
    map.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    map.put(TracingBuilder.OVERRIDE_PREFIX + "acks", "all");
    TracingConfiguration config = new TracingConfiguration(map);
    // When
    Sender sender = new TracingBuilder.SenderBuilder(config).build();
    // Then
    assertThat(sender).isInstanceOf(KafkaSender.class);
  }
}
