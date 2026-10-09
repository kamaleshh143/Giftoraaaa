# --- Build stage: compile and package the WAR with Maven ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first for faster rebuilds
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

# Build the application
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage: deploy the WAR on Tomcat 9 (Servlet 4 / javax) ---
FROM tomcat:9.0-jdk17-temurin

ENV CATALINA_OPTS="-Xms256m -Xmx384m" \
    GIFTORA_DB_DIR=/data \
    GIFTORA_SEED=true

# Remove the default Tomcat sample apps so our app owns the root context
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy the WAR built above as the ROOT application (context path "/")
COPY --from=build /app/target/giftora.war /usr/local/tomcat/webapps/ROOT.war

# Install entrypoint that maps Tomcat's port to $PORT
COPY entrypoint.sh /usr/local/tomcat/bin/giftora-entrypoint.sh
RUN chmod +x /usr/local/tomcat/bin/giftora-entrypoint.sh && mkdir -p /data

EXPOSE 8080
ENTRYPOINT ["/usr/local/tomcat/bin/giftora-entrypoint.sh"]
