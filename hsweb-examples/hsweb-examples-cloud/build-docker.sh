#!/usr/bin/env bash
mvn clean package
cd hsweb-examples-cloud-gateway
mvn docker:build
cd ../hsweb-examples-cloud-service01
mvn docker:build
cd ../hsweb-examples-cloud-user-center
mvn docker:build
