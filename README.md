# Dynatrace Troubleshooting Lab

This application is designed as a training tool to demonstrate various performance issues that can be analyzed with Dynatrace monitoring tools. It provides a control panel to generate different types of performance problems on demand.

## Features

- **Memory Leak Generator**: Create different types of memory leaks to analyze with memory dumps
- **CPU Load Generator**: Generate high CPU usage with various algorithms
- **Exception Generator**: Trigger various types of exceptions
- **Process Crash Simulator**: Cause the application to crash in different ways
- **Database Performance**: Demonstrate inefficient database queries and connection leaks

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Tomcat 8/9 (optional - can use embedded Tomcat)
- Docker and Docker Compose (for containerized deployment)

## Building and Running the Application

### Using Docker Compose (Recommended)

1. Clone this repository
2. Navigate to the project directory
3. Build and start the containers:

```bash
docker-compose up --build
```

The application will be available at: http://localhost:8080/troubleshooting-lab

To stop the application:
```bash
docker-compose down
```

### Manual Build and Run

1. Clone this repository
2. Navigate to the project directory
3. Build the WAR file using Maven:

```bash
mvn clean package
```

#### Using Embedded Tomcat

The easiest way to run the application is to use the embedded Tomcat server:

```bash
mvn tomcat7:run
```

The application will be available at: http://localhost:8080/troubleshooting-lab

#### Deploying to a Standalone Tomcat

1. Build the WAR file as described above
2. Copy the generated WAR file from `target/troubleshooting-lab.war` to your Tomcat's `webapps` directory
3. Start Tomcat
4. The application will be available at: http://localhost:8080/troubleshooting-lab

## Usage Instructions

1. Open the web interface in your browser
2. Navigate to the different panels for each type of performance issue
3. Configure the parameters as desired
4. Click the action buttons to trigger the selected issue
5. Use Dynatrace to monitor and analyze the resulting problems

## Security Warning

This application is intentionally designed to cause performance problems and should **NEVER** be deployed in a production environment. It's meant for training and testing purposes only, in controlled environments.

## Monitoring with Dynatrace

To get the most out of this application, install a Dynatrace OneAgent on the server running the application. This will allow you to:

1. Analyze memory leaks using memory dumps
2. Profile CPU usage
3. View exception statistics and details
4. Track database performance issues
5. Monitor application crashes and restarts

## Docker Configuration

The application is configured to run in Docker containers with the following setup:

- **Application Container**: Runs Tomcat 9 with the application deployed
- **Networks**: Uses a dedicated bridge network for container communication
- **Volumes**: 
  - `./data`: Application data directory mounted from the host

### Environment Variables

- `JAVA_OPTS`: JVM options for the application container

## License

This project is licensed under the MIT License - see the LICENSE file for details. # dt-profiling-demo
# dt-profiling-demo
