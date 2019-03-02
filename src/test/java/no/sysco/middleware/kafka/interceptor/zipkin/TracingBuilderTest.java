package no.sysco.middleware.kafka.interceptor.zipkin;

import brave.sampler.Sampler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.CommonClientConfigs;
import org.junit.Test;
import zipkin2.codec.Encoding;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.okhttp3.OkHttpSender;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TracingBuilderTest {

	@Test
	public void shouldBuildDefaultEncoding() {
		// Given
		Map<String, String> map = new HashMap<>();
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Encoding encoding = new TracingBuilder.EncodingBuilder(config).build();
		// Then
		Encoding defaultEncoding = Encoding
				.valueOf(TracingBuilder.EncodingBuilder.ENCODING_DEFAULT);
		assertEquals(defaultEncoding, encoding);
	}

	@Test
	public void shouldBuildEncoding() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.EncodingBuilder.ENCODING_CONFIG, Encoding.PROTO3.name());
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Encoding encoding = new TracingBuilder.EncodingBuilder(config).build();
		// Then
		assertEquals(Encoding.PROTO3, encoding);
	}

	@Test
	public void shouldBuildDefaultSampler() {
		// Given
		Map<String, String> map = new HashMap<>();
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sampler sampler = new TracingBuilder.SamplerBuilder(config).build();
		// Then
		float defaultSampler = Float
				.parseFloat(TracingBuilder.SamplerBuilder.SAMPLER_RATE_DEFAULT);
		assertEquals(Sampler.create(defaultSampler), sampler);
	}

	@Test
	public void shouldBuildSampler() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.SamplerBuilder.SAMPLER_RATE_CONFIG, "0.5");
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sampler sampler = new TracingBuilder.SamplerBuilder(config).build();
		// Then
		assertNotNull(sampler);
	}

	@Test
	public void shouldBuildSamplerWithFallback() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.SamplerBuilder.SAMPLER_RATE_CONFIG, "1.5");
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sampler sampler = new TracingBuilder.SamplerBuilder(config).build();
		// Then
		assertNotNull(sampler);
	}

	@Test
	public void shouldBuildNullSender() {
		// Given
		Map<String, String> map = new HashMap<>();
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sender sender = new TracingBuilder.SenderBuilder(config).build();
		// Then
		assertNull(sender);
	}

	@Test
	public void shouldBuildNoneSender() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.SenderBuilder.SENDER_TYPE_CONFIG,
				TracingBuilder.SenderBuilder.SenderType.NONE.name());
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sender sender = new TracingBuilder.SenderBuilder(config).build();
		// Then
		assertNull(sender);
	}

	@Test
	public void shouldBuildHttpSender() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.SenderBuilder.SENDER_TYPE_CONFIG,
				TracingBuilder.SenderBuilder.SenderType.HTTP.name());
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sender sender = new TracingBuilder.SenderBuilder(config).build();
		// Then
		assertTrue(sender instanceof OkHttpSender);
	}

	@Test
	public void shouldBuildKafkaSenderWithConfig() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.SenderBuilder.SENDER_TYPE_CONFIG,
				TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
		map.put(TracingBuilder.KafkaSenderBuilder.KAFKA_BOOTSTRAP_SERVERS_CONFIG,
				"localhost:9092");
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sender sender = new TracingBuilder.SenderBuilder(config).build();
		// Then
		assertTrue(sender instanceof KafkaSender);
	}

	@Test
	public void shouldBuildKafkaSenderWithDefault() {
		// Given
		Map<String, String> map = new HashMap<>();
		map.put(TracingBuilder.SenderBuilder.SENDER_TYPE_CONFIG,
				TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
		map.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sender sender = new TracingBuilder.SenderBuilder(config).build();
		// Then
		assertTrue(sender instanceof KafkaSender);
	}

	@Test
	public void shouldBuildKafkaSenderWithList() {
		// Given
		Map<String, Object> map = new HashMap<>();
		map.put(TracingBuilder.SenderBuilder.SENDER_TYPE_CONFIG,
				TracingBuilder.SenderBuilder.SenderType.KAFKA.name());
		map.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
				Arrays.asList("localhost:9092", "localhost:9094"));
		TracingConfiguration config = new TracingConfiguration(map);
		// When
		Sender sender = new TracingBuilder.SenderBuilder(config).build();
		// Then
		assertTrue(sender instanceof KafkaSender);
	}

}
