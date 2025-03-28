FROM maven:3.8.3-openjdk-17 as build
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD pom.xml $HOME
RUN mvn verify --fail-never
ADD . $HOME
RUN mvn clean package -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim
ENV TZ="Europe/Madrid"
COPY --from=build /usr/app/target/SonarrQueueFixer-1.0-jar-with-dependencies.jar /app/runner.jar
ENTRYPOINT java -jar /app/runner.jar