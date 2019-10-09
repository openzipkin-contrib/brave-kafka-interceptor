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

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
