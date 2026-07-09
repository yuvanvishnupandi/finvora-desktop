# 💎 Finvora - Finance Tracker

![Banner](https://img.shields.io/badge/Finance-Tracker-blue?style=for-the-badge&logo=java) ![JavaFX](https://img.shields.io/badge/JavaFX-Modern%20UI-orange?style=for-the-badge) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-Backend-brightgreen?style=for-the-badge)

Welcome to **Finvora**, an elite personal finance desktop application. Built with a modern Java tech stack, it provides an intuitive, seamless experience for tracking wealth, setting savings goals, and interacting with bleeding-edge AI models.

---

## ✨ Features

- **Multi-LLM AI Fallback Engine**
- **Voice-Activated AI Logging**
- **AI Receipt Scanner**
- **Dynamic Budgeting & Savings Goals**
- **Precision Time & Date Tracking**
- **PDF Report Generation**

---

## 🏗️ System Architecture 

Finvora utilizes a distinct **Client-Server Architecture** that separates the presentation layer (JavaFX) from the business logic and persistence layer (Spring Boot).

```mermaid
graph TD
    %% Client Side
    subgraph Client [Desktop Application - JavaFX]
        UI[Dynamic JavaFX Views & Dashboards]
        Controllers[UI Controllers]
        Models[Data Models]
        Services[AI Services Engine]
        
        UI <--> Controllers
        Controllers <--> Models
        Controllers <--> Services
        
        %% Internal Client Services
        subgraph AI [AI Routing Engine]
            Voice[AIVoiceService]
            Vision[AIVisionService]
            Router[AIEngine Intent Router]
            Voice & Vision --> Router
        end
        Services --- AI
    end

    %% External APIs
    subgraph External [External AI API Providers]
        APIKey[AI API Keys]
    end

    %% Server Side
    subgraph Server [Backend - Spring Boot]
        REST[REST Controllers]
        Logic[Service Layer]
        Data[Spring Data JPA]
        
        REST <--> Logic
        Logic <--> Data
    end

    %% Database
    subgraph DB [Embedded Database]
        H2[(H2 Relational DB)]
    end

    %% Connections
    Router -- Fallback Logic --> APIKey
    
    Controllers -- HTTP / JSON --> REST
    Data <--> H2
    
    style UI fill:#ff9900,stroke:#333,stroke-width:2px,color:#000
    style REST fill:#85ea2d,stroke:#333,stroke-width:2px,color:#000
    style H2 fill:#4285F4,stroke:#333,stroke-width:2px,color:#fff
    style AI fill:#9d4edd,stroke:#333,stroke-width:2px,color:#fff
```

---

## 🛠 Tech Stack

### Frontend (Client)
- **JavaFX**
- **Maven**
- **Gson**
- **Apache PDFBox**
- **JavaFX MediaPlayer**

### Backend (Server)
- **Spring Boot (Java 17)**
- **Spring Data JPA & Hibernate**
- **H2 Database**

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.9+

### 1. Boot up the Backend Server
Open a terminal window and run the background server logic:
```powershell
cd expense-tracker-springboot-server
mvn spring-boot:run
```

### 2. Launch the Desktop Application
Open a new terminal window and launch the user interface:
```powershell
cd expense-tracker-client
mvn compile javafx:run
```

---

## 📄 License
This project is licensed under the MIT License.
