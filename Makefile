include .env-default
export $(shell sed 's/=.*//' .env-default)

infra-up:
	(echo "Collector infrastructure UP"; docker-compose -f docker-compose.yaml up --build -d; echo "Waiting start all services for apply settings..."; sleep 50;)
	@make setup
	@make elastic-infra-up

infra-down:
	(echo "Collector infrastructure DOWN"; docker-compose -f docker-compose.yaml down -v)
	@make elastic-infra-down

elastic-infra-up:
	(echo "ElasticSearch infrastructure UP"; docker-compose -f docker-compose-elastic.yaml up --build -d;)

elastic-infra-down:
	(echo "Elasticsearch infrastructure DOWN"; docker-compose -f docker-compose-elastic.yaml down -v)

test:
	@make infra-up
	mvn clean test

build:
	mvn -DskipTests package --batch-mode

run:
	mvn clean spring-boot:run

run-collector-outbox-mysql:
	mvn -DskipTests package --batch-mode
	java -jar ./target/collector-0.0.1-SNAPSHOT.jar --spring.application.connector=collector-outbox-mysql

run-collector-outbox-mysql-with-kafka:
	mvn -DskipTests package --batch-mode
	java -jar ./target/collector-0.0.1-SNAPSHOT.jar --spring.application.connector=collector-outbox-mysql-with-kafka

setup:
	@make setup-mysql-outbox-table
	@make setup-kafka-topics-table

setup-kafka-topics-table:
	(cd infra; sh setup-kafka-topics-table.sh)

setup-mysql-outbox-table:
	(docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -Dmysql" < ./migrations/database-history.sql)
	(docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -Dmysql" < ./migrations/offset-store.sql)
	(docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -Dmysql" < ./migrations/outbox.sql)

send-random-event-mysql:
	(docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -Doutbox" < ./infra/sandbox/random-insert.sql)

mysql:
	(docker exec -it mysql.outbox.collector.dev mysql -uroot -proot -Doutbox)
