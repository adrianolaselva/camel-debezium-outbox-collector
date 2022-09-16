# OpenSearch

## Run OpenSearch With Docker

Example running OpenSearch.

```shell
docker run -p 9200:9200 -p 9600:9600 -e "discovery.type=single-node" opensearchproject/opensearch:latest
```

Run the command below to validate.

```shell
curl -XGET https://localhost:9200 -u admin:admin --insecure
```

Should return a json similar to the one below.

```json
{
  "name" : "aab3d136768e",
  "cluster_name" : "docker-cluster",
  "cluster_uuid" : "9Xw4GdY-S0S_-99RPbXwHA",
  "version" : {
    "distribution" : "opensearch",
    "number" : "2.3.0",
    "build_type" : "tar",
    "build_hash" : "6f6e84ebc54af31a976f53af36a5c69d474a5140",
    "build_date" : "2022-09-09T00:07:24.896263462Z",
    "build_snapshot" : false,
    "lucene_version" : "9.3.0",
    "minimum_wire_compatibility_version" : "7.10.0",
    "minimum_index_compatibility_version" : "7.0.0"
  },
  "tagline" : "The OpenSearch Project: https://opensearch.org/"
}
```