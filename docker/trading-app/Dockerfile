# https://stackoverflow.com/questions/51679363/multi-module-maven-project-on-dockers
# cache as most as possible in this multistage dockerfile.
FROM eclipse-temurin:17.0.12_7-jdk as DEPS

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY ../trading-app/pom.xml trading-app/pom.xml
COPY ../postgres-flyway/pom.xml postgres-flyway/pom.xml
COPY ../trading-data-generator/pom.xml trading-data-generator/pom.xml
COPY ../trading-logic/pom.xml trading-logic/pom.xml
COPY ../trading-api/pom.xml trading-api/pom.xml
COPY ../common/pom.xml common/pom.xml
COPY ../fibonacci-levels/pom.xml fibonacci-levels/pom.xml
COPY ../clickhouse-flyway/pom.xml clickhouse-flyway/pom.xml
COPY ../csv-processor/pom.xml csv-processor/pom.xml
COPY ../bybit-data-grabber/pom.xml bybit-data-grabber/pom.xml
COPY ../websocket-server-jakarta/pom.xml websocket-server-jakarta/pom.xml

# you get the idea:
# COPY moduleN/pom.xml moduleN/pom.xml

COPY ../pom.xml .
RUN mvn -B -e -C org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline

# if you have modules that depends each other, you may use -DexcludeArtifactIds as follows
# RUN mvn -B -e -C org.apache.maven.plugins:maven-dependency-plugin:3.1.2:go-offline -DexcludeArtifactIds=module1


# Use the official Maven image to build the application
FROM eclipse-temurin:17.0.12_7-jdk AS build

# Set the working directory
WORKDIR /app

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

COPY --from=deps /root/.m2 /root/.m2
COPY --from=deps /app/ /app
COPY ../trading-app/src /app/trading-app/src
COPY ../postgres-flyway/src /app/postgres-flyway/src
COPY ../trading-data-generator/src /app/trading-data-generator/src
COPY ../trading-logic/src /app/trading-logic/src
COPY ../trading-api/src /app/trading-api/src
COPY ../common/src /app/common/src
COPY ../fibonacci-levels/src /app/fibonacci-levels/src
COPY ../clickhouse-flyway/src /app/clickhouse-flyway/src
COPY ../csv-processor/src /app/csv-processor/src
COPY ../bybit-data-grabber/src /app/bybit-data-grabber/src
COPY ../websocket-server-jakarta/src /app/websocket-server-jakarta/src

# use -o (--offline) if you didn't need to exclude artifacts.
# if you have excluded artifacts, then remove -o flag
#RUN mvn -B -e -o clean install -DskipTests=true
RUN mvn -B -e clean install -DskipTests=true

# Use a lightweight JRE image to run the application
FROM eclipse-temurin:17.0.12_7-jre

# Set the working directory for the runtime
WORKDIR /app

# At this point, BUILDER stage should have your .jar or whatever in some path
COPY --from=build /app/trading-app/target/trading-app-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD [ "java", "-jar", "/app/app.jar" ]