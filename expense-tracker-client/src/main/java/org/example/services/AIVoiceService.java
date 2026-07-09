package org.example.services;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Consumer;

public class AIVoiceService {

    private static final String API_KEY = "YOUR_OPENAI_API_KEY";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private TargetDataLine targetDataLine;
    private File tempWavFile;
    private MediaPlayer mediaPlayer;
    private Thread recordingThread;

    public void startRecording() {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Microphone not supported.");
                return;
            }
            targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
            targetDataLine.open(format);
            targetDataLine.start();

            tempWavFile = File.createTempFile("voice_record", ".wav");
            
            recordingThread = new Thread(() -> {
                try (AudioInputStream audioStream = new AudioInputStream(targetDataLine)) {
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, tempWavFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            recordingThread.setDaemon(true);
            recordingThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecordingAndProcess(String userContext, String personalityMode, Consumer<String> statusUpdater, Consumer<String> resultUpdater, Consumer<String> intentUpdater) {
        if (targetDataLine != null) {
            targetDataLine.stop();
            targetDataLine.close();
        }
        
        if (recordingThread != null) {
            try {
                recordingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Task<Void> processTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    updateUI(statusUpdater, "Transcribing audio (The Ear)...");
                    String transcript = transcribeAudio(tempWavFile);
                    if (transcript == null || transcript.isBlank()) {
                        updateUI(statusUpdater, "Error: No speech detected.");
                        return null;
                    }
                    
                    runAIWorkflow(transcript, userContext, personalityMode, statusUpdater, resultUpdater, intentUpdater);
                } catch (Exception e) {
                    e.printStackTrace();
                    updateUI(statusUpdater, "Pipeline Error: " + e.getMessage());
                }
                return null;
            }
        };

        Thread bgThread = new Thread(processTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }

    public void processTextDirectly(String text, String userContext, String personalityMode, Consumer<String> statusUpdater, Consumer<String> resultUpdater, Consumer<String> intentUpdater) {
        Task<Void> processTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    runAIWorkflow(text, userContext, personalityMode, statusUpdater, resultUpdater, intentUpdater);
                } catch (Exception e) {
                    e.printStackTrace();
                    updateUI(statusUpdater, "Pipeline Error: " + e.getMessage());
                }
                return null;
            }
        };

        Thread bgThread = new Thread(processTask);
        bgThread.setDaemon(true);
        bgThread.start();
    }
    
    private void runAIWorkflow(String text, String userContext, String personalityMode, Consumer<String> statusUpdater, Consumer<String> resultUpdater, Consumer<String> intentUpdater) throws Exception {
        updateUI(statusUpdater, "Finvora AI: Routing Intent...");
        String agent1Prompt = "You are an AI intent router. The CURRENT LOCAL DATE is: " + java.time.LocalDate.now().toString() + " and CURRENT LOCAL TIME is: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")) + "\n" +
            "Analyze the user's input and classify their intent into one of three categories: ADD_TRANSACTION, NAVIGATION, or CONVERSATION.\n" +
            "If ADD_TRANSACTION: output JSON {\"intent\": \"ADD_TRANSACTION\", \"data\": {\"amount\": 50, \"category\": \"Food\", \"type\": \"expense\", \"name\": \"Lunch\", \"date\": \"YYYY-MM-DD\", \"time\": \"HH:mm AM/PM\"}}\n" +
            "CRITICAL DATE RULES: ALWAYS resolve relative dates (like 'yesterday', 'today', 'last week') using the CURRENT LOCAL DATE provided above. Do NOT output past years like 2023 unless explicitly stated by the user.\n" +
            "CRITICAL TIME RULES: Attempt to deduce the time. If no specific time is mentioned, default to the CURRENT LOCAL TIME provided above.\n" +
            "If NAVIGATION (only for explicit commands to open menus like 'open budgets' or 'show categories'): output JSON {\"intent\": \"NAVIGATION\", \"data\": {\"target\": \"BUDGETS\"}} (target can be BUDGETS, CATEGORIES, GOALS, REPORT)\n" +
            "If CONVERSATION (for ANY question, greeting, or advice request like 'how much money...', 'what is my balance', 'hello'): output JSON {\"intent\": \"CONVERSATION\", \"data\": {}}\n" +
            "CRITICAL: Do NOT route questions to NAVIGATION. Questions must be CONVERSATION.\n" +
            "Output ONLY raw JSON. No markdown formatting or extra text.";
        String intentJson = AIEngine.generateText(agent1Prompt, text);
        
        Platform.runLater(() -> intentUpdater.accept(intentJson));

        updateUI(statusUpdater, "Finvora AI: Crafting Strategic Advice...");
        
        String personalityInstruction = "Speak naturally with professional charm.";
        if ("Roast Mode 🔥".equals(personalityMode)) {
            personalityInstruction = "You are an aggressive, ruthless financial coach. Roast the user for their bad spending habits. Be extremely sarcastic, brutally honest, and funny. Be mean but helpful.";
        } else if ("Hype Man 🚀".equals(personalityMode)) {
            personalityInstruction = "You are a highly energetic hype man. Celebrate the user's financial choices, get them super pumped about their savings, and speak with extreme enthusiasm and slang!";
        }
        
        String agent2Prompt = "You are Finvora AI, an elite, highly intelligent personal financial advisor and assistant. " + personalityInstruction + "\n" +
            "You possess vast knowledge in personal finance, global economics, current events, and mathematical reasoning.\n" +
            "The user's LIVE financial context (balances, goals, budgets) is provided here: " + userContext + ".\n" +
            "CRITICAL INSTRUCTION: Read the user's input carefully and understand their exact intent. If they ask a specific question, answer it DIRECTLY, RELEVANTLY, and COMPREHENSIVELY.\n" +
            "If they ask about their data (like how much more is needed for a trip), do the exact math using the live context and give them a precise, accurate answer.\n" +
            "If they just want to chat, be a great conversationalist.\n" +
            "Always be highly intelligent, deeply context-aware, and extremely helpful. Never give a generic response when a specific one is possible.";
            
        String advice = AIEngine.generateText(agent2Prompt, text);
        
        if (advice.startsWith("ERROR")) {
            advice = "I'm sorry, I'm currently experiencing a brain freeze! All my AI providers are down.";
        }
        
        updateUI(resultUpdater, advice);

        updateUI(statusUpdater, "Finvora Speech Module: Generating Voice...");
        File audioFile = generateSpeech(advice);

        updateUI(statusUpdater, "Voice Financial Planner Ready.");
        playAudio(audioFile);
    }

    private void updateUI(Consumer<String> updater, String text) {
        Platform.runLater(() -> updater.accept(text));
    }

    private String transcribeAudio(File audioFile) throws Exception {
        String scriptPath = new File("stt.py").getAbsolutePath();
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath, audioFile.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append(" ");
        }
        p.waitFor();
        
        String result = output.toString().trim();
        if (result.startsWith("Error:") || result.isEmpty() || result.equals("No file")) {
            throw new Exception("Local STT Error: " + result);
        }
        return result;
    }

    private File generateSpeech(String text) throws Exception {
        // Google TTS fails with HTTP 400 if the query string is too long (> ~200 chars)
        if (text.length() > 190) {
            text = text.substring(0, 190);
        }
        
        String encodedText = java.net.URLEncoder.encode(text, StandardCharsets.UTF_8);
        String urlString = "https://translate.google.com/translate_tts?ie=UTF-8&client=tw-ob&tl=en&q=" + encodedText;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlString))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() != 200) {
            throw new Exception("Google TTS Error: HTTP " + response.statusCode());
        }

        File mp3 = File.createTempFile("advice", ".mp3");
        Files.write(mp3.toPath(), response.body());
        return mp3;
    }

    private void playAudio(File audioFile) {
        Platform.runLater(() -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }
            mediaPlayer = new MediaPlayer(new Media(audioFile.toURI().toString()));
            mediaPlayer.play();
        });
    }

    private String escapeJson(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private String extractJsonValue(String json, String key) {
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
