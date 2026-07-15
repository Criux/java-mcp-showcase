# --- build stage ---
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
# Resolve dependencies first so they land in a cacheable image layer
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package
# Extract the Boot layered jar so runtime layers cache independently
RUN java -Djarmode=tools -jar target/*.jar extract --layers --launcher --destination /extracted

# --- runtime stage ---
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 spring
USER spring
# layer order: least -> most frequently changing
COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
