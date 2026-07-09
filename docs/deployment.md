# Deployment Staging & Production

Because Finvora is a local desktop application built on JavaFX and an embedded Spring Boot H2 instance, "Deployment" typically refers to packaging the application for end-users rather than hosting it on a cloud server.

## Packaging the Backend (Spring Boot)

The backend must be packaged as a standalone executable JAR file.

```bash
cd expense-tracker-springboot-server
mvn clean package -DskipTests
```
This generates `expense-tracker-springboot-server-0.0.1-SNAPSHOT.jar` in the `target/` directory. 
The end user runs it via:
```bash
java -jar expense-tracker-springboot-server-0.0.1-SNAPSHOT.jar
```

## Packaging the Frontend (JavaFX)

To package the JavaFX application so that users do not need Maven installed, use the `jlink` or `jpackage` tools.

```bash
cd expense-tracker-client
mvn clean javafx:jlink
```
This produces a custom Java Runtime Environment (JRE) bundled with your application inside `target/image/`. You can zip this directory and distribute it to Windows users. They can simply double-click the provided launcher script/executable to start the client.

## Future Cloud Migration

If you decide to make the backend accessible over the internet (e.g., via AWS, Heroku, or GCP):
1. **Swap the Database**: Change the `application.properties` to connect to a cloud PostgreSQL or MySQL instance instead of H2.
2. **Dockerize**: Wrap the Spring Boot server in a `Dockerfile` and deploy to a container runtime like AWS ECS or Google Cloud Run.
3. **Update Client**: Modify the base URL in the JavaFX client's `SqlUtil.java` to point to the new remote server IP instead of `localhost`.
