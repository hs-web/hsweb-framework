FROM zh.sqy/java7:v1.0
RUN mkdir -p /apps/hsweb

COPY target/hsweb-web-run-1.0-SNAPSHOT.jar /apps/hsweb/hsweb-run.jar
EXPOSE 8088
ENTRYPOINT java -jar /apps/hsweb/hsweb-run.jar