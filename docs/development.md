# Developer Onboarding & Style Guidelines

This document provides a guide for new developers on how to navigate the codebase and adhere to the project's coding standards.

## Workspace Layout
- `expense-tracker-client/`: Contains the JavaFX UI source code.
  - `src/main/java/org/example/`: Java sources.
  - `src/main/resources/`: Images, CSS, and application data.
- `expense-tracker-springboot-server/`: Contains the Spring Boot backend.
  - `src/main/java/com/example/expense_tracker/`: Java sources.
  - `data/`: The local H2 database files.

## Style Guidelines
1. **No FXML**: All JavaFX Views must be constructed programmatically in pure Java to ensure type safety and high runtime performance.
2. **MVC Pattern**: Strict adherence to MVC. Controllers should never hold complex domain logic; delegate to `Services` and `Utils`.
3. **Naming**: Use camelCase for methods and variables, PascalCase for classes.
4. **Commenting**: Provide Javadoc for public API boundaries. Inline comments should only explain *why* something is done, not *what* is done.
