# ============================================================
# Water Quality Sampling Portal — multi-stage Dockerfile
# Stage 1: Maven build  |  Stage 2: Tomcat 10.1 runtime
# App: Spring Boot 3.3.5 WAR, Java 21, context-path /water-quality-portal
# ============================================================

# ---------- Stage 1: builder ----------
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build/water-quality-portal

# Copy POM first for better layer caching, then sources
COPY water-quality-portal/pom.xml ./pom.xml
COPY water-quality-portal/src ./src

# Package WAR, skip tests here (CI runs them separately)
RUN mvn -B package -DskipTests

# ---------- Stage 2: runtime ----------
FROM tomcat:10.1-jdk21-temurin

# Remove default Tomcat apps so only our portal is deployed
RUN rm -rf webapps/*

# Deploy the WAR built in stage 1 under the required context path
COPY --from=builder /build/water-quality-portal/target/water-quality-portal.war webapps/water-quality-portal.war

# Runtime configuration (overridable at `docker run -e` / compose)
ENV SPRING_PROFILES_ACTIVE=prod \
    DB_URL=jdbc:postgresql://db:5432/waterquality \
    DB_USER=wquser \
    DB_PASS=wqpass \
    TOMCAT_USER=admin

EXPOSE 8080

# Container health probe: Spring Boot Actuator endpoint
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/water-quality-portal/actuator/health || exit 1

# Tomcat default entrypoint (catalina.sh run) is inherited
