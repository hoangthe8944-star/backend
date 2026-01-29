package com.example.beatboxcompany.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:3000")
public class AIController {

    // Nếu bạn muốn dùng key từ application.properties thì đổi groqKey = apiKey bên dưới
    @Value("${ai.api.key}")
    private String apiKey;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithAI(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        
        // BƯỚC 1: Dán API Key Groq của bạn vào đây
        // Lấy tại: https://console.groq.com/keys
        String groqKey = "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"; 
        
        String url = "https://api.groq.com/openai/v1/chat/completions";

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // BƯỚC 2: Sử dụng HttpHeaders của Spring (org.springframework.http.HttpHeaders)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqKey);

            // BƯỚC 3: Cấu trúc Body theo chuẩn OpenAI/Groq
            Map<String, Object> body = new HashMap<>();
            // Model llama-3.3-70b-versatile rất mạnh và có hạn ngạch miễn phí lớn
            body.put("model", "llama-3.3-70b-versatile"); 
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "Bạn là trợ lý ảo của ứng dụng nghe nhạc BeatBox. Hãy trả lời thân thiện bằng tiếng Việt."));
            messages.add(Map.of("role", "user", "content", userMessage));
            
            body.put("messages", messages);

            // BƯỚC 4: Tạo Entity (Dùng org.springframework.http.HttpEntity)
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            // BƯỚC 5: Gửi Request
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // BƯỚC 6: Bóc tách kết quả từ cấu trúc của Groq/OpenAI
            if (response.getBody() != null && response.getBody().containsKey("choices")) {
                List choices = (List) response.getBody().get("choices");
                Map firstChoice = (Map) choices.get(0);
                Map message = (Map) firstChoice.get("message");
                String aiReply = (String) message.get("content");

                return ResponseEntity.ok(Map.of("reply", aiReply));
            } else {
                return ResponseEntity.ok(Map.of("reply", "AI không trả về kết quả."));
            }

        } catch (Exception e) {
            System.err.println("Lỗi gọi Groq: " + e.getMessage());
            return ResponseEntity.ok(Map.of("reply", "Hệ thống AI đang bận (Lỗi: " + e.getMessage() + ")"));
        }
    }
}