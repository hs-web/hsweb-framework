#!/usr/bin/env bash
#mvn clean package -Pprod
container_name=hsweb-web-run
image_name=hsweb/web-run
link_oracle=oracle11g
server_port=9888
if [ -f "target/hsweb-web-run-1.0-SNAPSHOT.jar" ]; then
        container_id=$(docker ps | grep "${container_name}" | awk '{print $1}')
        if [ "container_id" != "" ];then
            docker stop ${container_name}
            docker rm ${container_name}
            docker rmi  ${image_name}
        fi
            docker build -t ${image_name} .
            docker run -d --link ${link_oracle}:oracle -p ${server_port}:8088 -p 5005:5005 --name ${container_name} ${image_name}
           # docker run -it --rm --link oracle11gxe:oracle -p ${server_port}:8088 -p 5005:5005 --name ${container_name} ${image_name}
    else
        echo "build error!"
        exit -1
fi
