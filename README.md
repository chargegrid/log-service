# Log Service

The Log Service's main 2 responsibilities are:

- Storing OCPP log messages
- Expose a way for OCPP log messages to be queried

**log-service** receives OCPP log messages from central-system over RabbitMQ
and stores them in an Elasticsearch cluster. It exposes an API for
querying the log messages that basically allows passing through queries
to Elastichsearch directly, but with an added filter based on the tenant
of the authenticated user. So users can only see log messages of their
own tenant's charge boxes.

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Running

To run this application:

    lein run

## Kibana

Kibana is a UI for Elasticsearch that makes it easy to search indexed
documents. This is convenient for debugging purposes. You can run Kibana
and hook it up to Elasticsearch like this:

```
docker run -p 5601:5601 --net shareddevenv_default \
    -e ELASTICSEARCH_URL=http://es:9200 \
    -d kibana
```

We will not expose Kibana to end-users in production, so it's not necessary
perse to run Kibana in production.
