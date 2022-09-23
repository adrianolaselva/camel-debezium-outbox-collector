# Camel Outbox Collector

Exemplo de solução utilizando Debezium com apache camel para manter sincrizadas informações de base de dados de produção com elasticsearch para buscar.

Esta POC tem como objetivo ser um material de apoio para apresentar uma abordagem simplória em que dependêndo do cenário pode ser de grande valia, como por exemplo aliviar consultas em banco de dados produtivo e possibilitar consultas mais complexas utilizando outras soluções. No exemplo construído esta sendo utilizada uma instância de ElasticSearch.

## Arquitetura

![project](docs/images/debezium-connector-camel.drawio.png)

## Infra UP

```shell
make infra-up
```

## Infra DOWN

```shell
make infra-down
```

## Setup

```shell
make infra-setup
```