#!/usr/bin/env bash
mvn clean package -Pprod
docker build -t hsweb:test .
docker run -it --rm --link oracle11gxe:oracle -p 80:8088 hsweb:test