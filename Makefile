MAVEN := ./mvnw

.PHONY: all
all: build

.PHONY: license-header
license-header:
	${MAVEN} com.mycila:license-maven-plugin:format

.PHONY: build
build: license-header
	${MAVEN} clean package

.PHONY: docker-up
docker-up: build
	docker-compose up -d

.PHONY: pg-table
pg-table:
	docker-compose exec postgres psql -U postgres -c 'CREATE TABLE source_table (id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL)'

.PHONY: kafka-connectors
kafka-connectors:
	curl -XPUT -H 'Content-Type:application/json' -d @docker/kafka-connector/jdbc-source.json http://localhost:8083/connectors/jdbc_source/config
	curl -XPUT -H 'Content-Type:application/json' -d @docker/kafka-connector/jdbc-sink.json http://localhost:8083/connectors/jdbc_sink/config

.PHONY: pg-row
pg-row:
	docker-compose exec postgres psql -U postgres -c 'INSERT INTO source_table ( name ) VALUES (MD5(random()::text))'

.PHONY: ksql-stream
ksql-stream:
	docker-compose exec ksql-cli bash -c \
		'echo -e "\
		CREATE STREAM source_stream (id BIGINT, name VARCHAR) WITH (KAFKA_TOPIC='"'"'jdbc_source_table'"'"', VALUE_FORMAT='"'"'AVRO'"'"'); \
		SHOW STREAMS;" \
		| ksql http://ksql-server:8088'

.PHONY: ksql-select
ksql-select:
	docker-compose exec ksql-cli bash -c \
		'echo -e "\
		CREATE STREAM another_stream \
		AS SELECT id, name \
		FROM source_stream \
		EMIT CHANGES;" \
		| ksql http://ksql-server:8088'

