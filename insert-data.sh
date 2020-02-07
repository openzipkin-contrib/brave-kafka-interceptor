docker run --tty \
           --network brave-kafka-interceptor_default \
           --env PGDATABASE=postgres \
           --env PGUSER=postgres \
           --env PGPASSWORD=example \
           --env PGHOST=postgres \
           postgres \
           psql -c 'INSERT INTO source_table ( name ) VALUES (MD5(random()::text))'
