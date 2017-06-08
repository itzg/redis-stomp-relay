
[![CircleCI](https://circleci.com/gh/itzg/redis-stomp-relay.svg?style=svg)](https://circleci.com/gh/itzg/redis-stomp-relay)
[ ![Download](https://api.bintray.com/packages/itzgeoff/artifacts/redis-stomp-relay/images/download.svg) ](https://bintray.com/itzgeoff/artifacts/redis-stomp-relay)


This list Spring Boot application implements a STOMP relay that delegates to 
[Redis pub/sub](http://redis.io/topics/pubsub). 

**NOTE: this project is a work in progress and some aspects of the STOMP protocol are not yet implemented -- contributions are very welcome**

## Run in the CLI:

    mvn \
	-Dspring.redis.host=172.17.0.1 \
	-Dspring.redis.password=my#super#strong#pass \
	-Dspring.redis.port=6379 \
	-Dstomp-redis-relay.channel-prefix=/stomp/  \
	spring-boot:run


## Run with docker:

    docker run \
        -p=61613:61613 \
        -e SPRING_REDIS_HOST="172.17.0.1" \
        -e SPRING_REDIS_PASSWORD="my#super#strong#pass" \
        -e SPRING_REDIS_PORT=6379 \
        -e STOMP-REDIS-RELAY_CHANNEL-PREFIX='/stomp/' \
        --name=redis-stomp-relay \
        -d redis-stomp-relay

The image exposes the standard STOMP port of 61613 and connects to a Redis instance.

## Some considerations

All the variables are self explanatory except maybe `stomp-redis-relay.channel-prefix`. This *must* be conherent with the value you configure in your client's `StompBrokerRelay` (1)

Additional Redis connection properties can be set by passing `--spring.redis.` prefixed properties (2)

## External references

(1) [Spring Boot STOMP docs](https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-stomp-handle-broker-relay)

(2) [Spring Boot Redis properties](http://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/autoconfigure/data/redis/RedisProperties.html)
