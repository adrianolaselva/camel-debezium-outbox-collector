#!/bin/sh
set -e

docker-compose -f docker-compose.yaml exec -T mysql.outbox.collector.dev sh -c "mysql -uroot -proot -outbox" < ./mysql/random-insert.sql
