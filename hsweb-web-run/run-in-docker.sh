#!/usr/bin/env bash
#mvn clean package -Pprod
if [ -f "target/hsweb-web-run-1.0-SNAPSHOT.jar" ]; then
        container_id=$(docker ps | grep "hsweb-web-run" | awk '{print $1}')
        if [ "container_id" != "" ];then
            docker stop hsweb/web-run
            docker rmi  hsweb/web-run
        fi
            docker build -t hsweb/web-run .
            docker run -d --link oracle11g:oracle -p 9888:8088 -p 5005:5005 --name hsweb hsweb/web-run
           # docker run -it --rm --link oracle11gxe:oracle -p 80:8088 -p 5005:5005 --name hsweb hsweb/web-run
    else
        echo "build error!"
        exit -1
fi
