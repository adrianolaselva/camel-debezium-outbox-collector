# Camel Outbox Collector

Exemplo de solução utilizando Debezium com apache camel para manter sincrizadas informações de base de dados de produção com elasticsearch para buscar.

Esta POC tem como objetivo ser um material de apoio para apresentar uma abordagem simplória em que dependêndo do cenário pode ser de grande valia, como por exemplo aliviar consultas em banco de dados produtivo e possibilitar consultas mais complexas utilizando outras soluções. No exemplo construído esta sendo utilizada uma instância de ElasticSearch.

A implementação conta também com um aggregator para construir os lotes par apersistir os dados de forma mais performática e também os quebrando em índices no Elasticsearch de acordo com a data de criação (ex: `events-2022-01`).


<p align="center" width="100%">
    <img width="100%" src="docs/images/debezium-connector-camel.drawio.png"> 
</p>


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

<p align="center" width="100%">
    <img width="80%" src="docs/images/mysql-terminal.png"> 
</p>

Exemplo de instrução de insert

```sql
insert into outbox (id, message, created_at) values (UUID(), '{"name":"example","uuid":"ee8699ef-bbbc-4e20-91ff-4579690dae55","created_at":"2022-08-09T20:30:48.908+00:00","properties":{}}', now());
```


| Coluna                   | Descrição                                  | Exemplo                                                                           |
|--------------------------|--------------------------------------------|-----------------------------------------------------------------------------------|
| id                       | Identificador formato UUID                 | ee8699ef-bbbc-4e20-91ff-4579690dae55                                              |
| message                  | Mensagem json que será indexada no elastic | `{"name":"example","created_at":"2022-08-09T20:30:48.908+00:00","properties":{}}` |
| created_at               | Data de criação do registro                | 2022-08-09T20:30:48.908+00:00                                                     |


Consultar dados no ElasticSearch

```html
GET /events-*/_search
{
  "query": {
    "match_all": {}
  }
}
```

```json
{
  "took" : 1170,
  "timed_out" : false,
  "_shards" : {
    "total" : 1,
    "successful" : 1,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 23,
      "relation" : "eq"
    },
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "bc5b2af7-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:16:30+0000",
          "properties" : { },
          "id" : "bc5b2af7-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "bcb900eb-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:16:30+0000",
          "properties" : { },
          "id" : "bcb900eb-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "bd130c63-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:16:31+0000",
          "properties" : { },
          "id" : "bd130c63-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "bd6af728-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:16:31+0000",
          "properties" : { },
          "id" : "bd6af728-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "be097a4d-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:16:32+0000",
          "properties" : { },
          "id" : "be097a4d-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "be88729a-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:16:33+0000",
          "properties" : { },
          "id" : "be88729a-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "d5f5bff8-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:17:12+0000",
          "properties" : { },
          "id" : "d5f5bff8-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "d6552e8d-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:17:13+0000",
          "properties" : { },
          "id" : "d6552e8d-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "d6ab7ab7-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:17:14+0000",
          "properties" : { },
          "id" : "d6ab7ab7-3ae5-11ed-9123-02420a000005"
        }
      },
      {
        "_index" : "events-2022-09",
        "_type" : "_doc",
        "_id" : "d6fed3b9-3ae5-11ed-9123-02420a000005",
        "_score" : 1.0,
        "_source" : {
          "name" : "example",
          "uuid" : "ee8699ef-bbbc-4e20-91ff-4579690dae55",
          "created_at" : "2022-09-23T02:17:14+0000",
          "properties" : { },
          "id" : "d6fed3b9-3ae5-11ed-9123-02420a000005"
        }
      }
    ]
  }
}

```

## Referências

- [Apache Camel](https://camel.apache.org/)
- [Camel Debezium Mysql Component](https://camel.apache.org/components/3.18.x/debezium-mysql-component.html)
- [Camel Elasticsearch Component](https://camel.apache.org/components/3.18.x/elasticsearch-rest-component.html)