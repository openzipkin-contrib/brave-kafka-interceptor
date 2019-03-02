package no.sysco.middleware.kafka.interceptor.zipkin;

import brave.Tracing;
import brave.sampler.Sampler;
import org.apache.kafka.clients.CommonClientConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin2.Span;
import zipkin2.codec.Encoding;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class TracingBuilder {

	public static final String LOCAL_SERVICE_NAME_CONFIG = "zipkin.local.service.name";

	public static final String LOCAL_SERVICE_NAME_DEFAULT = "kafka-client";

	public static final String REMOTE_SERVICE_NAME_CONFIG = "zipkin.remote.service.name";

	public static final String REMOTE_SERVICE_NAME_DEFAULT = "kafka";

	public static final String TRACE_ID_128BIT_ENABLED_CONFIG = "zipkin.trace.id.128bit.enabled";

	public static final String TRACE_ID_128BIT_ENABLED_DEFAULT = "true";

	static final Logger LOGGER = LoggerFactory
			.getLogger(no.sysco.middleware.kafka.interceptor.zipkin.TracingBuilder.class);

	final String localServiceName;

	final String remoteServiceName;

	final boolean traceId128Bit;

	final TracingConfiguration configuration;

	public TracingBuilder(TracingConfiguration configuration) {
		this.configuration = configuration;
		this.localServiceName = configuration.getStringOrDefault(
				LOCAL_SERVICE_NAME_CONFIG, LOCAL_SERVICE_NAME_DEFAULT);
		this.remoteServiceName = configuration.getStringOrDefault(
				REMOTE_SERVICE_NAME_CONFIG, REMOTE_SERVICE_NAME_DEFAULT);
		String traceIdEnabledValue = configuration.getStringOrDefault(
				TRACE_ID_128BIT_ENABLED_CONFIG, TRACE_ID_128BIT_ENABLED_DEFAULT);
		this.traceId128Bit = Boolean.valueOf(traceIdEnabledValue);
	}

	public Tracing build() {
		Tracing.Builder builder = Tracing.newBuilder();
		Sender sender = new SenderBuilder(configuration).build();
		if (sender != null) {
			AsyncReporter<Span> reporter = AsyncReporter.builder(sender).build();
			builder.spanReporter(reporter);
		}
		Sampler sampler = new SamplerBuilder(configuration).build();
		return builder.sampler(sampler).localServiceName(localServiceName)
				.traceId128Bit(traceId128Bit).build();
	}

	public static class SenderBuilder {

		public static final String SENDER_TYPE_CONFIG = "zipkin.sender.type";

		public static final String SENDER_TYPE_DEFAULT = SenderType.NONE.name();

		final SenderType senderType;

		final TracingConfiguration configuration;

		public SenderBuilder(TracingConfiguration configuration) {
			String senderTypeValue = configuration.getStringOrDefault(SENDER_TYPE_CONFIG,
					SENDER_TYPE_DEFAULT);
			this.senderType = SenderType.valueOf(senderTypeValue);
			this.configuration = configuration;
		}

		public Sender build() {
			Encoding encoding = new EncodingBuilder(configuration).build();
			switch (senderType) {
			case HTTP:
				return new HttpSenderBuilder(configuration).build(encoding);
			case KAFKA:
				return new KafkaSenderBuilder(configuration).build(encoding);
			case NONE:
				return null;
			default:
				throw new IllegalArgumentException("Zipkin sender type unknown");
			}
		}

		enum SenderType {

			NONE, HTTP, KAFKA

		}

	}

	public static class HttpSenderBuilder {

		public static final String HTTP_ENDPOINT_CONFIG = "zipkin.http.endpoint";

		public static final String HTTP_ENDPOINT_DEFAULT = "http://localhost:9411/api/v2/spans";

		final String endpoint;

		public HttpSenderBuilder(TracingConfiguration configuration) {
			this.endpoint = configuration.getStringOrDefault(HTTP_ENDPOINT_CONFIG,
					HTTP_ENDPOINT_DEFAULT);
		}

		public Sender build(Encoding encoding) {
			return OkHttpSender.newBuilder().endpoint(endpoint).encoding(encoding)
					.build();
		}

	}

	public static class KafkaSenderBuilder {

		public static final String KAFKA_BOOTSTRAP_SERVERS_CONFIG = "zipkin.kafka.bootstrap.servers";

		final String bootstrapServers;

		public KafkaSenderBuilder(TracingConfiguration configuration) {
			this.bootstrapServers = configuration.getStringOrDefault(
					KAFKA_BOOTSTRAP_SERVERS_CONFIG,
					configuration.getStringOrDefault(
							CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
							configuration.getStringList(
									CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG)));
		}

		public Sender build(Encoding encoding) {
			return KafkaSender.newBuilder().bootstrapServers(bootstrapServers)
					.encoding(encoding).build();
		}

	}

	public static class EncodingBuilder {

		public static final String ENCODING_CONFIG = "zipkin.encoding";

		public static final String ENCODING_DEFAULT = "JSON";

		final Encoding encoding;

		public EncodingBuilder(TracingConfiguration configuration) {
			String encodingValue = configuration.getStringOrDefault(ENCODING_CONFIG,
					ENCODING_DEFAULT);
			encoding = Encoding.valueOf(encodingValue);
		}

		public Encoding build() {
			return encoding;
		}

	}

	public static class SamplerBuilder {

		public static final String SAMPLER_RATE_CONFIG = "zipkin.sampler.rate";

		public static final String SAMPLER_RATE_DEFAULT = "1.0F";

		public static final Float SAMPLER_RATE_FALLBACK = 0.0F;

		final Float rate;

		SamplerBuilder(TracingConfiguration configuration) {
			String rateValue = configuration.getStringOrDefault(SAMPLER_RATE_CONFIG,
					SAMPLER_RATE_DEFAULT);
			Float rate = Float.valueOf(rateValue);
			if (rate > 1.0 || rate <= 0.0 || rate.isNaN()) {
				rate = SAMPLER_RATE_FALLBACK;
				LOGGER.warn(
						"Invalid sampler rate {}, must be between 0 and 1. Falling back to {}",
						rate, SAMPLER_RATE_FALLBACK);
			}
			this.rate = rate;
		}

		public Sampler build() {
			return Sampler.create(rate);
		}

	}

}
