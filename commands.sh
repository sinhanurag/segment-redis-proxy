#!/bin/bash
echo $PWD
mvn clean install
mvn compile
mvn exec:java -Dexec.mainClass="com.segment.Application" -Dexec.args="--REDIS_HOST=127.0.0.1 --REDIS_PORT=6379 --GLOBAL_EXPIRY=60 --CACHE_SIZE=15 --PROXY_PORT=8080"