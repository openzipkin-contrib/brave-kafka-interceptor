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

/**
 * Keys to be tagged on spans created by interceptors. See {@link TracingConsumerInterceptor} and
 * {@link TracingProducerInterceptor}
 */
class KafkaInterceptorTagKey {
  static final String KAFKA_TOPIC = "kafka.topic";
  static final String KAFKA_KEY = "kafka.key";
  static final String KAFKA_CLIENT_ID = "kafka.client.id";
  static final String KAFKA_GROUP_ID = "kafka.group.id";
}
