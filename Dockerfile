# 基础镜像，使用 Eclipse Temurin Java 21 JRE
FROM eclipse-temurin:21-jre

ENV TZ=Asia/Shanghai
ENV JAVA_OPTS="-Xms128m -Xmx512m -Djava.security.egd=file:/dev/./urandom"

RUN ln -sf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /dpsm-api

WORKDIR /dpsm-api

EXPOSE 8080

ADD ./target/dpsm-api.jar ./

CMD java $JAVA_OPTS -jar dpsm-api.jar
