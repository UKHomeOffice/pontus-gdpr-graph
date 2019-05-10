#!/bin/bash

docker run --rm -it  -v $(pwd):/app -w /app maven:3.6-jdk-8-alpine mvn install
