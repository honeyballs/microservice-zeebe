# Build the Jar

FROM gradle:5.6-jdk11 AS builder

USER root

WORKDIR /builder

COPY . /builder

RUN gradle bootJar --stacktrace

# Build the executable image

FROM openjdk:11.0.4

COPY --from=builder /builder/build/libs/project-administration-0.0.1-SNAPSHOT.jar /usr/src/app/app.jar

WORKDIR /usr/src/app

EXPOSE 8086

ENV ZEEBE_URL host.docker.internal:26500

CMD ["java", "-jar", "app.jar"]