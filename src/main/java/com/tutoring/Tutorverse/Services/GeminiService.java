package com.tutoring.Tutorverse.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.tutoring.Tutorverse.Model.GeminiRequest;
import com.tutoring.Tutorverse.Model.GeminiResponse;

@Service
public class GeminiService {
    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.base:https://generativelanguage.googleapis.com/v1beta}")
    private String apiBase;

    @Value("${gemini.model:models/gemini-2.5-flash}")
    private String modelPath; // e.g. models/gemini-2.5-flash

    public String getGeminiResponse(String message) {
        if (message == null || message.isBlank()) {
            return "Message cannot be blank";
        }
        RestTemplate restTemplate = new RestTemplate();

        GeminiRequest request = new GeminiRequest(message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

    String fullUrl = buildGenerateUrl(modelPath);
    log.info("Calling Gemini model='{}' url='{}'", modelPath, fullUrl);

        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.exchange(fullUrl, HttpMethod.POST, entity, GeminiResponse.class);
            if (response.getBody() == null) {
                log.warn("Gemini response body null");
                return "No response";
            }
            String text = response.getBody().getText();
            if (text == null || text.isBlank()) {
                log.warn("Empty text in Gemini response. candidates={} model={}",
                        response.getBody().getCandidates() != null ? response.getBody().getCandidates().size() : 0,
                        modelPath);
            }
            return text;
        } catch (HttpStatusCodeException ex) {
            // Log full body for debugging but return concise error upstream
            log.error("Gemini API error status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            if (ex.getStatusCode().value() == 404 && modelPath.contains("1.5")) {
                String fallback = "models/gemini-2.5-flash";
                log.warn("Model '{}' not found (404). Attempting fallback '{}'", modelPath, fallback);
                try {
                    ResponseEntity<GeminiResponse> fb = restTemplate.exchange(buildGenerateUrl(fallback), HttpMethod.POST, entity, GeminiResponse.class);
                    return fb.getBody() != null ? fb.getBody().getText() : "No response (fallback)";
                } catch (Exception inner) {
                    log.error("Fallback call failed", inner);
                }
            }
            return "Gemini API error: " + ex.getStatusCode().value() + " - " + extractShortMessage(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.error("Unexpected error calling Gemini API", ex);
            return "Unexpected error calling Gemini API";
        }
    }

    private String buildGenerateUrl(String model) {
        return apiBase + "/" + model + ":generateContent?key=" + apiKey;
    }

    private String extractShortMessage(String body) {
        if (body == null) return "";
        int idx = body.indexOf("\"message\":");
        if (idx > -1) {
            String sub = body.substring(idx + 10);
            int quoteStart = sub.indexOf('"');
            if (quoteStart > -1) {
                sub = sub.substring(quoteStart + 1);
                int quoteEnd = sub.indexOf('"');
                if (quoteEnd > -1) return sub.substring(0, quoteEnd);
            }
        }
        return body.length() > 120 ? body.substring(0, 120) + "..." : body;
    }

    public String listModels() {
        RestTemplate restTemplate = new RestTemplate();
        String url = apiBase + "/models?key=" + apiKey;
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (HttpStatusCodeException ex) {
            log.error("List models error status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return "Error listing models: " + ex.getStatusCode().value() + " - " + extractShortMessage(ex.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Unexpected error listing models", e);
            return "Unexpected error listing models";
        }
    }
}
