# ---------- stage 1: extract layered  ----------
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /builder

ARG JAR_FILE=build/libs/teahub.jar
COPY ${JAR_FILE} application.jar

RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- stage 2: runtime ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /application

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

ENTRYPOINT ["java","-jar","application.jar"]
