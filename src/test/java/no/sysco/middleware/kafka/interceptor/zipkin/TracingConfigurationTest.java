package no.sysco.middleware.kafka.interceptor.zipkin;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TracingConfigurationTest {

	@Test
	public void shouldGetStringWhenValueExists() {
		// Given
		Map<String, String> configs = new HashMap<>();
		configs.put("k", "v");
		// When
		TracingConfiguration config = new TracingConfiguration(configs);
		String v = config.getString("k");
		// Then
		assertEquals("v", v);
	}

	@Test
	public void shouldGetNullStringWhenValueDoesNotExist() {
		// Given
		Map<String, String> configs = new HashMap<>();
		// When
		TracingConfiguration config = new TracingConfiguration(configs);
		String v = config.getString("k1");
		// Then
		assertNull(v);
	}

	@Test
	public void shouldGetDefaultWhenStringDoesNotExist() {
		// Given
		Map<String, String> configs = new HashMap<>();
		// When
		TracingConfiguration config = new TracingConfiguration(configs);
		String v = config.getStringOrDefault("k", "v");
		// Then
		assertEquals("v", v);
	}

	@Test
	public void shouldGetStringAndNotDefaultWhenValueExists() {
		// Given
		Map<String, String> configs = new HashMap<>();
		configs.put("k", "v");
		// When
		TracingConfiguration config = new TracingConfiguration(configs);
		String v = config.getStringOrDefault("k", "v1");
		// Then
		assertEquals("v", v);
	}

	@Test
	public void shouldGetStringListWhenValueExists() {
		// Given
		Map<String, List<String>> configs = new HashMap<>();
		configs.put("k", Arrays.asList("v", "v1"));
		// When
		TracingConfiguration config = new TracingConfiguration(configs);
		String v = config.getStringList("k");
		// Then
		assertEquals("v,v1", v);
	}

	@Test
	public void shouldGetNullWhenStringListValueDoesNotExist() {
		// Given
		Map<String, List<String>> configs = new HashMap<>();
		// When
		TracingConfiguration config = new TracingConfiguration(configs);
		String v = config.getStringList("k");
		// Then
		assertNull(v);
	}

}
