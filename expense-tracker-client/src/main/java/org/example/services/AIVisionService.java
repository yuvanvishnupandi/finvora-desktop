package org.example.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class AIVisionService {
    private static final String API_KEY = "y0T0kDv1MJLqckjfAjBO0Mu7li3P2Ojp";

    public static JsonObject processReceipt(File imageFile) throws Exception {
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);
        String mimeType = imageFile.getName().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
        String prompt = "Extract the following details from this receipt: totalAmount (number), vendorName (string), date (YYYY-MM-DD), and category (string). Return ONLY raw JSON in this format: {\"totalAmount\": 15.50, \"vendorName\": \"Starbucks\", \"date\": \"2024-01-01\", \"category\": \"Food\"}. Do not wrap it in markdown block, just output the raw json.";

        String content = AIEngine.processVision(base64Image, mimeType, prompt);

        if (content.startsWith("ERROR")) {
            throw new Exception(content);
        }

        // Remove markdown formatting if any
        if (content.startsWith("```json")) {
            content = content.substring(7);
            if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
            if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
        }
        
        return JsonParser.parseString(content.trim()).getAsJsonObject();
    }
}
