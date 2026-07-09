# System Architecture & Diagrams

Finvora utilizes a distinct **Client-Server Architecture** that separates the presentation layer (JavaFX) from the business logic and persistence layer (Spring Boot).

## High-Level Architecture Diagram

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

## Component Breakdown

### 1. JavaFX Client
- **Views**: Programmatic UI components rendering glassmorphic interfaces and responsive layouts. No FXML is used to guarantee runtime speed.
- **Controllers**: MVC bindings that manage interactions and delegate complex processing (like PDF generation or AI requests) to services.
- **AI Engine**: A highly available multi-provider API router that intelligently handles AI intent resolution and audio processing.

### 2. Spring Boot Server
- **REST Layer**: Secure, standard JSON-based HTTP API built with Spring Web.
- **Data Layer**: Hibernate ORM wrapping standard entity lifecycle management for Transactions, Budgets, and Goals.
- **H2 DB**: File-based zero-configuration database ensuring quick startup and isolation on the user's local machine.
