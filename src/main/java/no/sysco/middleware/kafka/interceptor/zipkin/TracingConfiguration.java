package no.sysco.middleware.kafka.interceptor.zipkin;

import java.util.AbstractList;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracing Configuration wraps properties provided by a Kafka Client and enable access to
 * configuration values.
 */
class TracingConfiguration {

	static final Logger LOGGER = LoggerFactory.getLogger(TracingConfiguration.class);

	final Map<String, ?> configs;

	TracingConfiguration(Map<String, ?> configs) {
		this.configs = configs;
	}

	String getStringList(String configKey) {
		final String value;
		final Object valueObject = configs.get(configKey);
		if (valueObject != null) {
			AbstractList valueList = (AbstractList) valueObject;
			value = String.join(",", valueList);
		}
		else {
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
		}
		else {
			LOGGER.warn("{} of type String is not found in properties", configKey);
			value = defaultValue;
		}
		return value;
	}

	String getString(String configKey) {
		return getStringOrDefault(configKey, null);
	}

}
