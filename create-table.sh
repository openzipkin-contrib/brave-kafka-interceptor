docker run --tty \
           --network brave-kafka-interceptor_default \
           --env PGDATABASE=postgres \
           --env PGUSER=postgres \
           --env PGPASSWORD=example \
           --env PGHOST=postgres \
           postgres \
           psql -c ' CREATE TABLE source_table ( id SERIAL PRIMARY KEY, name VARCHAR(100) NOT NULL);'
