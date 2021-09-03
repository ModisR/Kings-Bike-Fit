FROM openjdk:8-jdk-alpine
RUN apk add --no-cache bash nodejs
WORKDIR /opt/kings_bike_fit
CMD sbt/bin/sbt -Dsbt.ci=true run