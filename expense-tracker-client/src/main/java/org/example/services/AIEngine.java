package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class AIEngine {
    private static final String GEMINI_KEY = "YOUR_GEMINI_API_KEY";
    private static final String MISTRAL_KEY = "YOUR_MISTRAL_API_KEY";
    private static final String OPENAI_KEY = "YOUR_OPENAI_API_KEY";
    
    private static final HttpClient client = HttpClient.newHttpClient();

    public static String generateText(String systemPrompt, String userMessage) {
        System.out.println("AIEngine: Attempting Gemini 2.5 Flash...");
        try {
            return callGemini(systemPrompt + "\n\n" + userMessage);
        } catch (Exception e1) {
            System.err.println("Gemini failed: " + e1.getMessage());
            System.out.println("AIEngine: Falling back to Mistral Large...");
            try {
                return callMistral("mistral-large-latest", systemPrompt, userMessage);
            } catch (Exception e2) {
                System.err.println("Mistral failed: " + e2.getMessage());
                System.out.println("AIEngine: Falling back to OpenAI GPT-4o-mini...");
                try {
                    return callOpenAI("gpt-4o-mini", systemPrompt, userMessage);
                } catch (Exception e3) {
                    System.err.println("OpenAI failed: " + e3.getMessage());
                    return "ERROR: All AI Providers failed. Please check your internet or API quotas.";
                }
            }
        }
    }

    public static String processVision(String base64Image, String mimeType, String prompt) {
        System.out.println("AIEngine: Attempting Gemini 2.5 Flash Vision...");
        try {
            return callGeminiVision(base64Image, mimeType, prompt);
        } catch (Exception e1) {
            System.err.println("Gemini Vision failed: " + e1.getMessage());
            System.out.println("AIEngine: Falling back to Mistral Pixtral...");
            try {
                return callMistralVision("pixtral-12b-2409", base64Image, mimeType, prompt);
            } catch (Exception e2) {
                System.err.println("Mistral Vision failed: " + e2.getMessage());
                System.out.println("AIEngine: Falling back to OpenAI Vision...");
                try {
                    return callOpenAIVision("gpt-4o-mini", base64Image, mimeType, prompt);
                } catch (Exception e3) {
                    System.err.println("OpenAI Vision failed: " + e3.getMessage());
                    return "ERROR: All Vision Providers failed.";
                }
            }
        }
    }

    // --- GEMINI ---
    private static String callGemini(String prompt) throws Exception {
        String jsonPayload = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}], \"generationConfig\": {\"temperature\": 0.7}}",
                escapeJson(prompt)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception(response.body());
        return extractJsonValue(response.body(), "\"text\"");
    }

    private static String callGeminiVision(String base64Image, String mimeType, String prompt) throws Exception {
        JsonObject root = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject contentObj = new JsonObject();
        JsonArray parts = new JsonArray();
        
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        
        JsonObject inlineDataPart = new JsonObject();
        JsonObject inlineData = new JsonObject();
        inlineData.addProperty("mime_type", mimeType);
        inlineData.addProperty("data", base64Image);
        inlineDataPart.add("inline_data", inlineData);
        parts.add(inlineDataPart);
        
        contentObj.add("parts", parts);
        contents.add(contentObj);
        root.add("contents", contents);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_KEY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception(response.body());
        return extractJsonValue(response.body(), "\"text\"");
    }

    // --- MISTRAL ---
    private static String callMistral(String model, String sys, String user) throws Exception {
        String jsonPayload = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.7}",
                model, escapeJson(sys), escapeJson(user)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mistral.ai/v1/chat/completions"))
                .header("Authorization", "Bearer " + MISTRAL_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception(response.body());
        return extractJsonValue(response.body(), "\"content\"");
    }

    private static String callMistralVision(String model, String base64Image, String mimeType, String prompt) throws Exception {
        return callOaiCompatibleVision("https://api.mistral.ai/v1/chat/completions", MISTRAL_KEY, model, base64Image, mimeType, prompt);
    }

    // --- OPENAI ---
    private static String callOpenAI(String model, String sys, String user) throws Exception {
        String jsonPayload = String.format(
                "{\"model\":\"%s\",\"messages\":[{\"role\":\"system\",\"content\":\"%s\"},{\"role\":\"user\",\"content\":\"%s\"}],\"temperature\":0.7}",
                model, escapeJson(sys), escapeJson(user)
        );
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + OPENAI_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception(response.body());
        return extractJsonValue(response.body(), "\"content\"");
    }

    private static String callOpenAIVision(String model, String base64Image, String mimeType, String prompt) throws Exception {
        return callOaiCompatibleVision("https://api.openai.com/v1/chat/completions", OPENAI_KEY, model, base64Image, mimeType, prompt);
    }

    // --- UTILS ---
    private static String callOaiCompatibleVision(String url, String key, String model, String base64Image, String mimeType, String prompt) throws Exception {
        String dataUrl = "data:" + mimeType + ";base64," + base64Image;
        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        
        JsonArray messages = new JsonArray();
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        
        JsonArray contentArray = new JsonArray();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("type", "text");
        textObj.addProperty("text", prompt);
        contentArray.add(textObj);
        
        JsonObject imageObj = new JsonObject();
        imageObj.addProperty("type", "image_url");
        JsonObject imageUrlObj = new JsonObject();
        imageUrlObj.addProperty("url", dataUrl);
        imageObj.add("image_url", imageUrlObj);
        contentArray.add(imageObj);
        
        userMessage.add("content", contentArray);
        messages.add(userMessage);
        root.add("messages", messages);
        root.addProperty("max_tokens", 500);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + key)
                .POST(HttpRequest.BodyPublishers.ofString(root.toString(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) throw new Exception(response.body());
        return extractJsonValue(response.body(), "\"content\"");
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private static String extractJsonValue(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return "";
        int quoteStart = json.indexOf("\"", keyIndex + key.length() + 1);
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        while (json.charAt(quoteEnd - 1) == '\\') {
            quoteEnd = json.indexOf("\"", quoteEnd + 1);
        }
        return json.substring(quoteStart + 1, quoteEnd).replace("\\\"", "\"").replace("\\n", "\n");
    }
}
