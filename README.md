# Finvora
Finvora is a production-grade, AI-powered personal finance tracker and wealth management workspace. It features dynamic budgeting, savings goal tracking, and interactive AI financial assistance.

## 🌟 Features
- **Multi-LLM AI Fallback Engine:** Never experience downtime. Chat seamlessly with your AI advisor powered by a resilient engine routing between various AI APIs.
- **Voice-Activated AI Logging:** Inline voice prompting parses your spoken commands, grabs the exact time, and logs the transaction.
- **AI Receipt Scanner:** Upload JPEG or PDF receipts and watch the AI extract transaction names, amounts, and dates with zero manual entry.
- **Dynamic Budgeting & Savings Goals:** Set up custom savings goals and monitor monthly budgets with real-time visual progress bars.
- **Precision Time & Date Tracking:** Advanced TimePicker UI allows you to log exactly when transactions happen.
- **PDF Report Generation:** Export beautiful, structured PDF financial reports directly from your dashboard.

## 🏗️ Quick Architecture Summary
The application follows a decoupled client-server architecture:

- **Frontend:** Desktop application built on JavaFX, Maven, Gson, and Apache PDFBox.
- **Backend:** REST API server built on Spring Boot (Java 17), Spring Data JPA, and Hibernate.
- **Database:** Embedded H2 Database for rapid, lightweight persistence.
- **Core Integrations:** External AI API Keys for intelligent financial recommendations.

## 🛠️ Prerequisites
- JDK 17 or higher
- Maven 3.9+

## 🚀 Quick Local Setup

```bash
# Clone the repository
git clone https://github.com/yuvanvishnupandi/Finance_Tracker_APP.git
cd Finance_Tracker_APP

# Dependencies are automatically downloaded by Maven during the compile step!
```

## ⚙️ Environment Setup

**Backend (`expense-tracker-springboot-server/src/main/resources/application.properties`)**
```properties
server.port=8080
spring.datasource.url=jdbc:h2:file:./data/expense_tracker_db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true
```

**Frontend AI APIs (`expense-tracker-client/src/main/java/org/example/services/AIEngine.java`)**
Ensure you inject your API keys here before compiling:
```java
private static final String GEMINI_KEY = "YOUR_GEMINI_API_KEY";
private static final String MISTRAL_KEY = "YOUR_MISTRAL_API_KEY";
private static final String OPENAI_KEY = "YOUR_OPENAI_API_KEY";
```

## 💻 Quick Run Instructions

Start the backend API server:
```powershell
cd expense-tracker-springboot-server
mvn spring-boot:run
```

Start the frontend JavaFX application (in a new terminal):
```powershell
cd expense-tracker-client
mvn compile javafx:run
```

## 🧪 Testing Commands

Run the Maven test suites:

```powershell
# Run backend tests
cd expense-tracker-springboot-server
mvn test

# Run frontend tests
cd expense-tracker-client
mvn test
```

## 📦 Deployment Summary

Deploy the backend as a standard Spring Boot executable JAR and the frontend as a bundled JavaFX executable. 

```powershell
# Build the backend JAR
cd expense-tracker-springboot-server
mvn clean package
java -jar target/expense-tracker-springboot-server-0.0.1-SNAPSHOT.jar
```

## 📖 Documentation Index (Deep Guides)
All detailed architectural, deployment, and API design specifications live inside the project structure:

- **System Architecture & Diagrams**
- **API Reference REST**
- **Deployment Staging & Production**
- **Developer Onboarding & Style Guidelines**
- **Security Policy & Verification**
- **Testing Strategy & local testing**
- **Troubleshooting Common Issues**
- **FAQ**

## 🤝 Contribution Quickstart
We welcome open-source contributions! Please review our Contributing Guide and adhere to our Code of Conduct before opening Pull Requests.

## 📄 License
This project is licensed under the MIT License.
