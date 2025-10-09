package com.tutoring.Tutorverse.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.tutoring.Tutorverse.Services.GeminiService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping
    public String chat(@Valid @RequestBody MessageRequest messageRequest) {
        String msg = messageRequest.getMessage();
        if (msg == null || msg.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field 'message' is required and must not be blank. Use {\"message\":\"Your prompt\"} as the JSON body.");
        }
        return geminiService.getGeminiResponse(msg.trim());
    }

    @GetMapping("/models")
    public String listModels() {
        return geminiService.listModels();
    }

    @GetMapping("/config")
    public ConfigResponse config() {
        // Simple reflection of environment-backed values (no secrets)
        return new ConfigResponse();
    }

    static class ConfigResponse {
        public String base = System.getProperty("gemini.api.base", System.getenv().getOrDefault("GEMINI_API_BASE", "(default)"));
        public String model = System.getProperty("gemini.model", System.getenv().getOrDefault("GEMINI_MODEL", "(default)"));
    }

    static class MessageRequest {
        @NotBlank(message = "message must not be blank")
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
