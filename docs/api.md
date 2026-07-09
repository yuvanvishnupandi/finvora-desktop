# API Reference REST

The Spring Boot backend exposes a standard RESTful API for the JavaFX client to communicate with the database.

## Base URL
`http://localhost:8080/api/v1`

## Endpoints

### 👤 Users
- **POST** `/user/register` - Register a new user
- **POST** `/user/login` - Authenticate a user

### 💸 Transactions
- **GET** `/transaction/user/{userId}` - Fetch all transactions for a user
- **POST** `/transaction` - Add a new transaction
- **PUT** `/transaction/{id}` - Update a transaction
- **DELETE** `/transaction/{id}` - Delete a transaction

### 🏷️ Categories
- **GET** `/category/user/{userId}` - Get all custom categories
- **POST** `/category` - Create a new category
- **DELETE** `/category/{id}` - Delete a category

### 📊 Budgets
- **GET** `/budget/user/{userId}` - Get all budgets for user
- **POST** `/budget` - Set or update a budget
- **DELETE** `/budget/{id}` - Delete a budget

### 🎯 Savings Goals
- **GET** `/goal/user/{userId}` - Get all savings goals
- **POST** `/goal` - Create a new savings goal
- **PUT** `/goal/{id}` - Update a savings goal
- **DELETE** `/goal/{id}` - Delete a savings goal
