# Troubleshooting Common Issues

### 1. Connection Refused Exception (Backend Down)
**Symptom**: When attempting to log in, you see an HTTP Connection Refused error in the terminal, or the UI hangs.
**Fix**: Ensure the Spring Boot backend server is running on port 8080. Open a terminal, navigate to `expense-tracker-springboot-server`, and run `mvn spring-boot:run`.

### 2. AI Responses Not Working
**Symptom**: Clicking the AI Voice or text buttons yields no response.
**Fix**: Your API keys in `AIEngine.java` or `AIVoiceService.java` might be missing or expired. Open those files, search for `YOUR_API_KEY`, and replace them with valid keys.

### 3. Port 8080 Already in Use
**Symptom**: Spring Boot fails to start with `Port 8080 was already in use`.
**Fix**: Kill the process occupying port 8080 or change `server.port` in `application.properties` to `8081`, and update the frontend `SqlUtil.java` base URL accordingly.
