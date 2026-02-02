# 基础镜像，可以先执行 docker pull openjdk:21-jdk-slim
FROM openjdk:21-jre-slim

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /dpsm-api

WORKDIR /dpsm-api

EXPOSE 8080

ADD ./target/dpsm-api.jar ./

CMD java $JAVA_OPTS -jar dpsm-api.jar
