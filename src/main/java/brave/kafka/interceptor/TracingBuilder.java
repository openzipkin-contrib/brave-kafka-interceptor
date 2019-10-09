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

import brave.Tracing;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import org.apache.kafka.clients.CommonClientConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka.KafkaSender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import static brave.kafka.interceptor.TracingConfiguration.ENCODING_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.ENCODING_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.HTTP_ENDPOINT_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.HTTP_ENDPOINT_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.KAFKA_BOOTSTRAP_SERVERS_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.LOCAL_SERVICE_NAME_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.LOCAL_SERVICE_NAME_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.SAMPLER_RATE_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.SAMPLER_RATE_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.SENDER_TYPE_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.SENDER_TYPE_DEFAULT;
import static brave.kafka.interceptor.TracingConfiguration.TRACE_ID_128BIT_ENABLED_CONFIG;
import static brave.kafka.interceptor.TracingConfiguration.TRACE_ID_128BIT_ENABLED_DEFAULT;

/**
 * Initialization of Zipkin Tracing components.
 */
class TracingBuilder {
  static final Logger LOGGER = LoggerFactory.getLogger(TracingBuilder.class);

  final String localServiceName;
  final boolean traceId128Bit;
  final TracingConfiguration configuration;

  TracingBuilder(TracingConfiguration configuration) {
    this.configuration = configuration;
    this.localServiceName =
      configuration.getStringOrDefault(LOCAL_SERVICE_NAME_CONFIG, LOCAL_SERVICE_NAME_DEFAULT);
    String traceIdEnabledValue = configuration.getStringOrDefault(TRACE_ID_128BIT_ENABLED_CONFIG,
      TRACE_ID_128BIT_ENABLED_DEFAULT);
    this.traceId128Bit = Boolean.parseBoolean(traceIdEnabledValue);
  }

  Tracing build() {
    Tracing.Builder builder = Tracing.newBuilder();
    Sender sender = new SenderBuilder(configuration).build();
    if (sender != null) {
      AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
      builder.spanReporter(reporter);
    }
    Sampler sampler = new SamplerBuilder(configuration).build();
    return builder.sampler(sampler)
      .localServiceName(localServiceName)
      .traceId128Bit(traceId128Bit)
      .build();
  }

  static class SenderBuilder {
    final SenderType senderType;
    final TracingConfiguration configuration;

    SenderBuilder(TracingConfiguration configuration) {
      String senderTypeValue =
        configuration.getStringOrDefault(SENDER_TYPE_CONFIG, SENDER_TYPE_DEFAULT);
      this.senderType = SenderType.valueOf(senderTypeValue);
      this.configuration = configuration;
    }

    Sender build() {
      Encoding encoding = new EncodingBuilder(configuration).build();
      switch (senderType) {
        case HTTP:
          return new HttpSenderBuilder(configuration).build(encoding);
        case KAFKA:
          return new KafkaSenderBuilder(configuration).build(encoding);
        case NONE:
          return null;
        default:
          throw new IllegalArgumentException("Zipkin sender type unknown");
      }
    }

    enum SenderType {
      NONE, HTTP, KAFKA
    }
  }

  static class HttpSenderBuilder {
    final String endpoint;

    HttpSenderBuilder(TracingConfiguration configuration) {
      this.endpoint = configuration.getStringOrDefault(HTTP_ENDPOINT_CONFIG, HTTP_ENDPOINT_DEFAULT);
    }

    Sender build(Encoding encoding) {
      return OkHttpSender.newBuilder().endpoint(endpoint).encoding(encoding).build();
    }
  }

  public static class KafkaSenderBuilder {

    final String bootstrapServers;

    KafkaSenderBuilder(TracingConfiguration configuration) {
      this.bootstrapServers = configuration.getStringOrDefault(
        KAFKA_BOOTSTRAP_SERVERS_CONFIG,
        configuration.getStringOrDefault(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
          configuration.getStringList(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG)));
    }

    Sender build(Encoding encoding) {
      return KafkaSender.newBuilder().bootstrapServers(bootstrapServers).encoding(encoding).build();
    }
  }

  static class EncodingBuilder {
    final Encoding encoding;

    EncodingBuilder(TracingConfiguration configuration) {
      String encodingValue = configuration.getStringOrDefault(ENCODING_CONFIG, ENCODING_DEFAULT);
      encoding = Encoding.valueOf(encodingValue);
    }

    Encoding build() {
      return encoding;
    }
  }

  static class SamplerBuilder {
    static final Float SAMPLER_RATE_FALLBACK = 0.0F;

    final Float rate;

    SamplerBuilder(TracingConfiguration configuration) {
      String rateValue =
        configuration.getStringOrDefault(SAMPLER_RATE_CONFIG, SAMPLER_RATE_DEFAULT);
      Float rate = Float.valueOf(rateValue);
      if (rate > 1.0 || rate <= 0.0 || rate.isNaN()) {
        rate = SAMPLER_RATE_FALLBACK;
        LOGGER.warn(
          "Invalid sampler rate {}, must be between 0 and 1. Falling back to {}",
          rate, SAMPLER_RATE_FALLBACK);
      }
      this.rate = rate;
    }

    Sampler build() {
      return Sampler.create(rate);
    }
  }
}
