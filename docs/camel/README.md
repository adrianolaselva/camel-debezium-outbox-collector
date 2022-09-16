# Debezium MySql Connector

Estrutura de eventos para tratamento a partir do camel.

### Example Struct Header Debezium Mysql Camel

```
{
   CamelDebeziumBefore=null, 
   CamelDebeziumDdlSQL=null, 
   CamelDebeziumIdentifier=collector_outbox.outbox.outbox, 
   CamelDebeziumKey=Struct{
      id=a5045fcd-35d9-11ed-983d-0242ac190004
   }, 
   CamelDebeziumOperation=c, 
   CamelDebeziumSourceMetadata={
      query=null, 
      thread=87, 
      server_id=184054, 
      version=1.9.4.Final, 
      sequence=null, 
      file=mysql-bin.000003, 
      connector=mysql, 
      pos=12998, 
      name=collector_outbox, 
      gtid=null, 
      row=0, 
      ts_ms=1663344441000, 
      snapshot=false,
      db=outbox, 
      table=outbox
   }, 
   CamelDebeziumTimestamp=1663346172475, 
   CamelMessageTimestamp=1663346172475
}
```

### Example Struct Body Debezium Mysql Camel

```
Struct{
    id=a5045fcd-35d9-11ed-983d-0242ac190004,
    message={
        "name":"example",
        "uuid":"ee8699ef-bbbc-4e20-91ff-4579690dae55",
        "created_at":"2022-08-09T20:30:48.908+00:00",
        "properties":{}
    },
    created_at=1663344441000
}
```