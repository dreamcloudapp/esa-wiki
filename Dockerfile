########Maven build stage########
FROM maven:3.6-jdk-11 as maven_build
WORKDIR /app

# Install the ESA score package
RUN git clone https://github.com/dreamcloudapp/esa-score esa-score
RUN --mount=type=cache,target=/root/.m2  mvn -f esa-score/pom.xml clean install -Dmaven.test.skip

# Install the ESA core package
RUN git clone https://github.com/dreamcloudapp/esa-core esa-core
RUN --mount=type=cache,target=/root/.m2  mvn -f esa-core/pom.xml clean install -Dmaven.test.skip

#copy pom
COPY pom.xml .

#copy source
COPY src ./src

# build the app and download dependencies only when these are new (thanks to the cache)
RUN --mount=type=cache,target=/root/.m2  mvn clean package -Dmaven.test.skip

########JRE run stage########
FROM openjdk:11.0-jre
WORKDIR /app

#copy the fat JAR as this is all we really need atm
RUN mkdir /app/target
COPY --from=maven_build /app/target/esa-wiki-1.0-jar-with-dependencies.jar /app/target
COPY esa.sh /app
COPY esa-process.sh /app
RUN mkdir /app/wiki
RUN mkdir /app/index

#run the app
CMD /bin/bash