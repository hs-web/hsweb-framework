FROM hsweb/java8

COPY target/hsweb-web-run-1.0-SNAPSHOT.jar /app.jar
EXPOSE 8088
ENTRYPOINT ["java","-Xdebug","-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005","-jar","/app.jar"]