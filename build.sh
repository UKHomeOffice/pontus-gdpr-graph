#!/bin/bash

docker run --rm -ti  $(pwd):/app -w /app maven:3.6-jdk-8-alpine mvn install
