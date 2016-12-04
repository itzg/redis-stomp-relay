
[![CircleCI](https://circleci.com/gh/itzg/redis-stomp-relay.svg?style=svg)](https://circleci.com/gh/itzg/redis-stomp-relay)
[ ![Download](https://api.bintray.com/packages/itzgeoff/artifacts/redis-stomp-relay/images/download.svg) ](https://bintray.com/itzgeoff/artifacts/redis-stomp-relay)


This list Spring Boot application implements a STOMP relay that delegates to 
[Redis pub/sub](http://redis.io/topics/pubsub). 

**NOTE: this project is a work in progress and some aspects of the STOMP protocol are not yet implemented -- contributions are very welcome**

## Using the Docker image

The image exposes the standard STOMP port of 61613 and connects to a Redis instance specified by `REDIS_HOST`.
The default for `REDIS_HOST` is `redis`.

Assuming you have a Redis container named `redis`, an example of typical usage would be:

    docker run -d -e REDIS_HOST=myredis --link myredis:redis -p 61613:61613 itzg/redis-stomp-relay

Additional Redis connection properties can be set by passing `--spring.redis.` prefixed properties from
[these declarations](http://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/data/redis/RedisProperties.html).