# syntax=docker/dockerfile:experimental

########Maven build stage########
FROM maven:3.6-jdk-11 as maven_build
WORKDIR /app

#copy pom
COPY pom.xml .

#copy source
COPY src ./src

# build the app and download dependencies only when these are new (thanks to the cache)
RUN --mount=type=cache,target=/root/.m2  mvn clean package -Dmaven.test.skip

# split the built app into multiple layers to improve layer rebuild
RUN mkdir -p target/docker-packaging && cd target/docker-packaging && jar -xf ../my-app*.jar

########JRE run stage########
FROM openjdk:11.0-jre
WORKDIR /app

#copy built app layer by layer
ARG DOCKER_PACKAGING_DIR=/app/target/docker-packaging
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/BOOT-INF/lib /app/lib
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/BOOT-INF/classes /app/classes
COPY --from=maven_build ${DOCKER_PACKAGING_DIR}/META-INF /app/META-INF

#run the app
CMD java java -Djdbc.drivers=org.postgresql.Driver -DtotalEntitySizeLimit=2147480000 -Djdk.xml.totalEntitySizeLimit=2147480000 -Xmx48g -jar esa-wiki-1.0-jar-with-dependencies.jar %*
