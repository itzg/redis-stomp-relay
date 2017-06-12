FROM java:8-jre-alpine

ENV APP_VERSION=0.0.2

ADD https://bintray.com/itzgeoff/artifacts/download_file?file_path=me%2Fitzg%2Fredis-stomp-relay%2F${APP_VERSION}%2Fredis-stomp-relay-${APP_VERSION}.jar /opt/redis-stomp-relay.jar

EXPOSE 61613

ENTRYPOINT ["/usr/bin/java","-jar","/opt/redis-stomp-relay.jar"]
