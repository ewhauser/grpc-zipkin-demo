#!/bin/bash -x

./gradlew run -Ddocker.ip=$(docker-machine ip default)
