MAVEN := ./mvnw

.PHONY: all
all: build

.PHONY: license-header
license-header:
	${MAVEN} com.mycila:license-maven-plugin:format

.PHONY: build
build: license-header
	${MAVEN} clean package

.PHONY: docker-kafka-connectors
docker-kafka-connectors:
	curl -XPUT -H 'Content-Type:application/json' -d @docker/kafka-connector/jdbc-source.json http://localhost:8083/connectors/jdbc_source/config
	curl -XPUT -H 'Content-Type:application/json' -d @docker/kafka-connector/jdbc-sink.json http://localhost:8084/connectors/jdbc_sink/config
