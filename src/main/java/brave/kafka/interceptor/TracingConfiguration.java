/*
 * Copyright 2018-2022 The OpenZipkin Authors
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

import java.util.AbstractList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracing Configuration wraps properties provided by a Kafka Client and enable access to
 * configuration values.
 */
public class TracingConfiguration {
  static final Logger LOGGER = LoggerFactory.getLogger(TracingConfiguration.class);

  public static final String SENDER_TYPE_CONFIG = "zipkin.sender.type";
  public static final String SENDER_TYPE_DEFAULT = "NONE";
  public static final String HTTP_ENDPOINT_CONFIG = "zipkin.http.endpoint";
  public static final String HTTP_ENDPOINT_DEFAULT = "http://localhost:9411/api/v2/spans";
  public static final String KAFKA_BOOTSTRAP_SERVERS_CONFIG = "zipkin.kafka.bootstrap.servers";
  public static final String LOCAL_SERVICE_NAME_CONFIG = "zipkin.local.service.name";
  public static final String LOCAL_SERVICE_NAME_DEFAULT = "kafka-client";
  public static final String REMOTE_SERVICE_NAME_CONFIG = "zipkin.remote.service.name";
  public static final String REMOTE_SERVICE_NAME_DEFAULT = "kafka";
  public static final String TRACE_ID_128BIT_ENABLED_CONFIG = "zipkin.trace.id.128bit.enabled";
  public static final String TRACE_ID_128BIT_ENABLED_DEFAULT = "true";
  public static final String ENCODING_CONFIG = "zipkin.encoding";
  public static final String ENCODING_DEFAULT = "JSON";
  public static final String SAMPLER_RATE_CONFIG = "zipkin.sampler.rate";
  public static final String SAMPLER_RATE_DEFAULT = "1.0F";
  public static final String KAFKA_OVERRIDE_PREFIX = "zipkin.kafka.";

  final Map<String, ?> configs;

  TracingConfiguration(Map<String, ?> configs) {
    this.configs = configs;
  }

  String getStringList(String configKey) {
    final String value;
    final Object valueObject = configs.get(configKey);
    if (valueObject instanceof AbstractList) {
      AbstractList valueList = (AbstractList) valueObject;
      value = String.join(",", valueList);
    } else {
      LOGGER.warn("{} of type ArrayList is not found in properties", configKey);
      value = null;
    }
    return value;
  }

  /**
   * @return Value as String. If not found, then null is returned.
   */
  String getStringOrDefault(String configKey, String defaultValue) {
    final String value;
    final Object valueObject = configs.get(configKey);
    if (valueObject instanceof String) {
      value = (String) valueObject;
    } else {
      LOGGER.warn("{} of type String is not found in properties", configKey);
      value = defaultValue;
    }
    return value;
  }

  String getString(String configKey) {
    return getStringOrDefault(configKey, null);
  }

  /**
   * @return Value as a string regardless of whether the type of the property is a String
   *         or a List (for instance `bootstrap.servers` property may have a List of servers
   *         instead of a single String). If not found, then null is returned.
   */
  String getStringOrStringList(String configKey) {
    final String value;
    final Object valueObject = configs.get(configKey);
    if (valueObject instanceof AbstractList) {
      value = getStringList(configKey);
    } else {
      value = getStringOrDefault(configKey, null);
    }
    return value;
  }

  Map<String, String> getKafkaOverrides() {
    Map<String, String> overrides = new HashMap<>();

    for (String key : getKeySet()) {
      if (!key.equals(KAFKA_BOOTSTRAP_SERVERS_CONFIG) && key.startsWith(KAFKA_OVERRIDE_PREFIX))
        copyConfig(overrides, key);
    }

    return overrides;
  }

  void copyConfig(Map<String, String> to, String key) {
    String value = getStringOrStringList(key);
    String kafkaKey = key.replace(KAFKA_OVERRIDE_PREFIX,"");
    if (value != null && value.length() > 0) {
      to.put(kafkaKey, value);
      LOGGER.info("Adding '{}:{}' property to kafka configuration", kafkaKey, value);
    }
  }

  Set<String> getKeySet() {
    return configs.keySet();
  }
}
