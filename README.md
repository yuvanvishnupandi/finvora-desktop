# Finvora - Finance Tracker 🚀

![Banner](https://img.shields.io/badge/Finance-Tracker-blue?style=for-the-badge&logo=java) ![JavaFX](https://img.shields.io/badge/JavaFX-Modern%20UI-orange?style=for-the-badge) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Backend-brightgreen?style=for-the-badge)

**Finvora** is an elite, robust desktop application built with a modern Java tech stack. It provides users with an intuitive, seamless experience in managing their personal finances. From tracking daily expenses and long-term savings goals to powerful AI-driven financial advice, the application centralizes financial data into a dynamic, user-friendly interface.

## ✨ Features

- **Intuitive Dashboard:** A central hub displaying current balance, recent transactions, and dynamic budget charts.
- **Transaction Management:** Easily add, edit, and delete income and expense records. Includes an advanced TimePicker for precise tracking.
- **Budgeting System:** Set up and monitor monthly budgets across different categories, with visual progress bars.
- **Savings Goals:** Create custom savings goals (e.g., "Manali Trip", "New Car") with dynamic, real-time progress tracking.
- **AI Financial Assistant:** Powered by an ultra-resilient Multi-LLM Fallback Engine (Gemini, Mistral, OpenAI). Chat via voice or text, ask for financial advice, or add transactions using natural language.
- **Proactive AI Monitoring:** Automatically scans for unused subscriptions and spending anomalies in the background.
- **AI Receipt Scanner:** Upload JPEGs or PDFs of your receipts and let the AI extract the data automatically.
- **Dual Themes:** Switch between modern dark mode and clean light mode seamlessly.
- **Report Generation:** Generate and export beautiful PDF financial reports.

## 🛠 Tech Stack

### Frontend (Client)
- **JavaFX:** UI rendering and view components.
- **Maven:** Build automation and dependency management.
- **Gson:** JSON parsing and serialization.
- **Apache PDFBox:** PDF report generation.
- **JavaFX MediaPlayer:** Audio playback for AI text-to-speech.

### Backend (Server)
- **Spring Boot (Java 17):** REST API framework.
- **Spring Data JPA & Hibernate:** ORM and database interactions.
- **H2 Database:** Embedded, file-based relational database for rapid, lightweight persistence.

## 🏗 System Architecture & Design

Finvora utilizes a distinct **Client-Server Architecture** separating the presentation layer from the business logic and persistence layer.

### 1. Client Architecture (JavaFX)
The frontend strictly adheres to the **MVC (Model-View-Controller)** design pattern:
- **Models (`org.example.models`):** POJOs representing `User`, `Transaction`, `Budget`, and `SavingsGoal`.
- **Views (`org.example.views`):** JavaFX programmatic layouts (no FXML) ensuring high performance and dynamic UI rendering.
- **Controllers (`org.example.controllers`):** Acts as the glue between Views and Models, handling user input and triggering API calls.
- **Services (`org.example.services`):** Advanced business logic like the `AIEngine`, `AIVoiceService`, and `AIVisionService`.
- **Utils (`org.example.utils`):** `SqlUtil` manages all REST API HTTP calls to the Spring Boot backend using standard `HttpURLConnection`.

### 2. Multi-LLM Resilient Fallback Engine
Finvora features a state-of-the-art AI routing system. The `AIEngine` securely manages multiple API keys across different providers. If a primary provider (e.g., Gemini) hits a rate limit or fails, the engine seamlessly and instantly falls back to the next provider (Mistral, then OpenAI), guaranteeing uninterrupted AI functionality for the user.

### 3. Server Architecture (Spring Boot)
The backend follows a standard **Controller-Service-Repository** pattern:
- **Controllers:** Expose RESTful endpoints (e.g., `/api/v1/transaction`).
- **Services:** Execute business logic and validation.
- **Repositories:** Interface with the H2 database via Spring Data JPA.

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.9+

### Running the Backend
1. Navigate to `expense-tracker-springboot-server`.
2. Run `mvn spring-boot:run`.
3. The server will start on `http://localhost:8080`.

### Running the Frontend
1. Navigate to `expense-tracker-client`.
2. Run `mvn compile javafx:run`.
3. The desktop client will launch. Register a new user and enjoy!

## 📄 License
This project is licensed under the MIT License.
