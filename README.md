[![Build Status](https://travis-ci.org/innoq/docster.svg?branch=master)](https://travis-ci.org/innoq/docster)

# Docster

Transforming proxy that creates an html represenation of your json hypermedia (e.g. HAL) api on the fly.

## Running instances

- Docster instance on Heroku: [https://docster-innoq.herokuapp.com](https://docster-innoq.herokuapp.com)
- Sample HAL API: [https://lit-woodland-47740.herokuapp.com/](https://lit-woodland-47740.herokuapp.com/)


## Start

```shell
SERVER_URI="my-server-base-uri" sbt run
```

or
```shell
sbt run -Dserver.uri="my-server-base-uri"
```

## Execute tests

```shell
sbt test
```


## Current Features

- transparent proxy
- HAL support:
  - displays relations
  - displays attributes
