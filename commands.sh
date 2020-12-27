#!/bin/bash
mvn clean install
mvn exec:java -e -Dexec.mainClass="com.segment.Application" -Dexec.args="--REDIS_HOST=redis --REDIS_PORT=6379 --GLOBAL_EXPIRY=60 --CACHE_SIZE=15 --PROXY_PORT=8080"