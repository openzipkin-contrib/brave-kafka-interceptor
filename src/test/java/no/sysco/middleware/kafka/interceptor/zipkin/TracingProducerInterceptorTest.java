package no.sysco.middleware.kafka.interceptor.zipkin;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;
import org.junit.Test;
import zipkin2.Span;

import static org.junit.Assert.assertNotNull;

public class TracingProducerInterceptorTest extends BaseTracingTest {

	private final ProducerRecord<String, String> record = new ProducerRecord<>("topic",
			"value");

	@Test
	public void shouldNotTouchRecords() {
		final TracingProducerInterceptor<String, String> interceptor = new TracingProducerInterceptor<>();
		interceptor.configure(map);
		final ProducerRecord<String, String> tracedRecord = interceptor.onSend(record);
		Assert.assertEquals(record, tracedRecord);
	}

	@Test
	public void shouldCreateSpanOnSend() {
		// Given
		final TracingProducerInterceptor<String, String> interceptor = new TracingProducerInterceptor<>();
		interceptor.configure(map);
		interceptor.tracing = tracing;
		// When
		interceptor.onSend(record);
		// Then
		final Span span = spans.getLast();
		assertNotNull(span);
	}

}
