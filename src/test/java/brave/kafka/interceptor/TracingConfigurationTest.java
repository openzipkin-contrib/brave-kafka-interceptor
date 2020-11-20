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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TracingConfigurationTest {

  @Test void shouldGetStringWhenValueExists() {
    // Given
    Map<String, String> configs = new HashMap<>();
    configs.put("k", "v");
    // When
    TracingConfiguration config = new TracingConfiguration(configs);
    // Then
    assertThat(config.getString("k")).isEqualTo("v");
  }

  @Test void shouldGetNullStringWhenValueDoesNotExist() {
    // Given
    Map<String, String> configs = new HashMap<>();
    // When
    TracingConfiguration config = new TracingConfiguration(configs);
    // Then
    assertThat(config.getString("k1")).isNull();
  }

  @Test void shouldGetDefaultWhenStringDoesNotExist() {
    // Given
    Map<String, String> configs = new HashMap<>();
    // When
    TracingConfiguration config = new TracingConfiguration(configs);
    // Then
    assertThat(config.getStringOrDefault("k", "v")).isEqualTo("v");
  }

  @Test void shouldGetStringAndNotDefaultWhenValueExists() {
    // Given
    Map<String, String> configs = new HashMap<>();
    configs.put("k", "v");
    // When
    TracingConfiguration config = new TracingConfiguration(configs);
    // Then
    assertThat(config.getStringOrDefault("k", "v1")).isEqualTo("v");
  }

  @Test void shouldGetStringListWhenValueExists() {
    // Given
    Map<String, List<String>> configs = new HashMap<>();
    configs.put("k", Arrays.asList("v", "v1"));
    // When
    TracingConfiguration config = new TracingConfiguration(configs);
    // Then
    assertThat(config.getStringList("k")).isEqualTo("v,v1");
  }

  @Test void shouldGetNullWhenStringListValueDoesNotExist() {
    // Given
    Map<String, List<String>> configs = new HashMap<>();
    // When
    TracingConfiguration config = new TracingConfiguration(configs);
    // Then
    assertThat(config.getStringList("k")).isNull();
  }
}
