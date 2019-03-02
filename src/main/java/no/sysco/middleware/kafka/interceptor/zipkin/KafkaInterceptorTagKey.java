package no.sysco.middleware.kafka.interceptor.zipkin;

/**
 * Keys to be tagged on spans created by interceptors. See {@link TracingConsumerInterceptor} and
 * {@link TracingProducerInterceptor}
 */
class KafkaInterceptorTagKey {

	static final String KAFKA_TOPIC = "kafka.topic";
	static final String KAFKA_KEY = "kafka.key";
	static final String KAFKA_CLIENT_ID = "kafka.client.id";
	static final String KAFKA_GROUP_ID = "kafka.group.id";

}
