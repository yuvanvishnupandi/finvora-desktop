# Security Policy & Verification

Protecting user financial data is paramount. 

## Authentication & Authorization
Currently, Finvora stores passwords locally via the Spring Boot Backend. For production rollout, passwords must be securely hashed using BCrypt before persisting to the database.

## External API Keys
The application integrates with Gemini, Mistral, and OpenAI. 
- **DO NOT** commit real API keys into version control (GitHub).
- Always rely on the fallback logic within the `AIEngine.java` to prevent application crashes if an API key is rate-limited or revoked.

## Database Security
The embedded H2 Database (`expense_tracker_db.mv.db`) is stored locally on the user's filesystem. Users are responsible for securing their local OS environment. Do not commit this `.db` file to GitHub as it contains personal financial logs.
