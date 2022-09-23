# Camel Outbox Collector

Exemplo de solução utilizando Debezium com apache camel para manter sincrizadas informações de base de dados de produção com elasticsearch para buscar.

Esta POC tem como objetivo ser um material de apoio para apresentar uma abordagem simplória em que dependêndo do cenário pode ser de grande valia, como por exemplo aliviar consultas em banco de dados produtivo e possibilitar consultas mais complexas utilizando outras soluções. No exemplo construído esta sendo utilizada uma instância de ElasticSearch.

A implementação conta também com um aggregator para construir os lotes par apersistir os dados de forma mais performática e também os quebrando em índices no Elasticsearch de acordo com a data de criação (ex: `events-2022-01`).


![project](docs/images/debezium-connector-camel.drawio.png)

## Bibliotecas.

| Library                  | Artifact                             |
|--------------------------|--------------------------------------|
| org.apache.camel         | camel-kafka                          |
| org.apache.camel         | camel-debezium-mysql                 |
| org.apache.camel         | camel-elasticsearch-rest             |
| org.elasticsearch.client | elasticsearch-rest-high-level-client |

## Infra UP

Comando para subir serviços e efetuar setup inicial da solução.

```shell
make infra-up
```

## Infra DOWN

Comando para parar todos os seviços.

```shell
make infra-down
```

## Acessar MySql

Comando para acessar instência MySql para gerar inserir dados e por sua vez gerar eventos para serem persistidos no elasticsearch.

```shell
make mysql
```

![project](docs/images/mysql-terminal.png)

Exemplo de instrução de insert

```sql
insert into outbox (id, message, created_at) values (UUID(), '{"name":"example","uuid":"ee8699ef-bbbc-4e20-91ff-4579690dae55","created_at":"2022-08-09T20:30:48.908+00:00","properties":{}}', now());
```


| Coluna                   | Descrição                                  |
|--------------------------|--------------------------------------------|
| id                       | Identificador formato UUID                 |
| message                  | Mensagem json que será indexada no elastic |
| created_at               | Data de criação do registro                |


## Referências

- [Apache Camel](https://camel.apache.org/)
- [Camel Debezium Mysql Component](https://camel.apache.org/components/3.18.x/debezium-mysql-component.html)
- [Camel Elasticsearch Component](https://camel.apache.org/components/3.18.x/elasticsearch-rest-component.html)