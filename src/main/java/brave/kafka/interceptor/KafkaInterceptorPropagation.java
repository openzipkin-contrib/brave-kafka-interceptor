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

import brave.propagation.Propagation.Getter;
import brave.propagation.Propagation.Setter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

/**
 * Propagation utilities to inject and extract context from {@link Header}
 */
final class KafkaInterceptorPropagation {
  static final Charset UTF_8 = StandardCharsets.UTF_8;

  static final Setter<Headers, String> HEADER_SETTER = (carrier, key, value) -> {
    carrier.remove(key);
    carrier.add(key, value.getBytes(UTF_8));
  };

  static final Getter<Headers, String> HEADER_GETTER = (carrier, key) -> {
    Header header = carrier.lastHeader(key);
    if (header == null) return null;
    return new String(header.value(), UTF_8);
  };

  KafkaInterceptorPropagation() {
  }
}
