# Testing Strategy & Local Testing

## Backend Testing (Spring Boot)
The backend tests rely on JUnit and Mockito.

To run the backend test suite:
```bash
cd expense-tracker-springboot-server
mvn test
```

## Frontend Testing (JavaFX)
Due to the nature of JavaFX programmatic views, unit testing involves verifying the state of Controllers and Models. UI integration tests can be run using the TestFX library (if added in the future).

To run current unit tests:
```bash
cd expense-tracker-client
mvn test
```
