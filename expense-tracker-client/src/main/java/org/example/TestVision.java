package org.example;
import org.example.services.AIVisionService;
import java.io.File;

public class TestVision {
    public static void main(String[] args) {
        try {
            System.out.println("Testing Vision...");
            File dummy = new File("dummy.png");
            if (!dummy.exists()) {
                java.nio.file.Files.write(dummy.toPath(), new byte[]{1,2,3,4,5});
            }
            com.google.gson.JsonObject result = AIVisionService.processReceipt(dummy);
            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
