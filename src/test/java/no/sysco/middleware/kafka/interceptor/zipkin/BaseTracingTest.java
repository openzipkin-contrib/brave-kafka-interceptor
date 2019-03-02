package no.sysco.middleware.kafka.interceptor.zipkin;

import brave.Tracing;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import zipkin2.Span;

class BaseTracingTest {

	ConcurrentLinkedDeque<Span> spans = new ConcurrentLinkedDeque<>();

	Tracing tracing = Tracing.newBuilder().spanReporter(spans::add).build();

	HashMap<String, Object> map = new HashMap<>();

	BaseTracingTest() {
		map.put("client.id", "client-1");
		map.put("group.id", "group-1");
	}

}
