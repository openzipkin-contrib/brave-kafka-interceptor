package no.sysco.middleware.kafka.interceptor.zipkin;

import brave.propagation.Propagation.Getter;
import brave.propagation.Propagation.Setter;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.Charset;

/**
 * Propagation utilities to inject and extract context from {@link Header}
 */
final class KafkaInterceptorPropagation {

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	static final Setter<Headers, String> HEADER_SETTER = (carrier, key, value) -> {
		carrier.remove(key);
		carrier.add(key, value.getBytes(UTF_8));
	};

	static final Getter<Headers, String> HEADER_GETTER = (carrier, key) -> {
		Header header = carrier.lastHeader(key);
		if (header == null)
			return null;
		return new String(header.value(), UTF_8);
	};

	KafkaInterceptorPropagation() {
	}

}
