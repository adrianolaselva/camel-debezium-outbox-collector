include .env-default
export $(shell sed 's/=.*//' .env-default)

infra-up:
	(echo "Collector infrastructure UP"; docker-compose -f docker-compose.yaml up --build -d)

infra-down:
	(echo "Collector infrastructure DOWN"; docker-compose -f docker-compose.yaml down -v)

test:
	@make infra-up
	mvn clean test

run:
	mvn clean spring-boot:run

setup:
	@make setup-mysql-outbox-table
	@make setup-kafka-topics-table

setup-kafka-topics-table:
	(cd infra; sh setup-kafka-topics-table.sh)

setup-mysql-outbox-table:
	(docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -Dmysql" < ./migrations/outbox.sql)

send-random-event-mysql:
	(docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -Doutbox" < ./infra/sandbox/random-insert.sql)