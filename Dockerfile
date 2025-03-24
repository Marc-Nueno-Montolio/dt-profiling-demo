# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11-slim AS builder

# Set the working directory
WORKDIR /app

# Copy only the pom.xml first
COPY pom.xml .

# Download dependencies - removing the offline mode flag
# First download the dependencies without building
RUN mvn dependency:go-offline dependency:resolve-plugins dependency:resolve -B

# Copy source files
COPY src ./src

# Build the application
# Removed offline mode (-o) since it was causing issues
RUN mvn clean package -DskipTests -B

# Stage 2: Run the application
FROM tomcat:9.0-jdk11-openjdk-slim

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file from builder stage
COPY --from=builder /app/target/troubleshooting-lab.war /usr/local/tomcat/webapps/ROOT.war

# Copy the manager app from the default webapps.dist directory
RUN cp -r /usr/local/tomcat/webapps.dist/manager /usr/local/tomcat/webapps/

# Configure Tomcat users
COPY src/main/webapp/WEB-INF/tomcat-users.xml /usr/local/tomcat/conf/tomcat-users.xml

# Configure manager app context to allow access from any IP
RUN echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<Context antiResourceLocking="false" privileged="true">\n\
  <Valve className="org.apache.catalina.valves.RemoteAddrValve" allow=".*" />\n\
</Context>' > /usr/local/tomcat/webapps/manager/META-INF/context.xml

# Create volume for Tomcat logs
VOLUME /usr/local/tomcat/logs

# Set Java options for better container support
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expose Tomcat's port
EXPOSE 8080

# Start Tomcat
CMD ["catalina.sh", "run"] 