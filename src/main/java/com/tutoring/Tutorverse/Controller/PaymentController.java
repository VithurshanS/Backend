package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Services.UserService;
import com.tutoring.Tutorverse.Dto.PaymentDto;
import com.tutoring.Tutorverse.Services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public Map<String, Object> createPayment(@RequestBody PaymentDto paymentreq, HttpServletRequest req) {
        UUID studentId = userService.getUserIdFromRequest(req);
        System.out.println("Creating payment for studentId: " + studentId);
        System.out.println("Creating payment for module: " + paymentreq.getModuleId() + " with amount: " + paymentreq.getAmount());
        if (studentId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid token");
        }
        return paymentService.createPayment(studentId, paymentreq.getModuleId(), paymentreq.getAmount());
    }

    @PostMapping("/notify")
    public String notifyHandler(@RequestParam MultiValueMap<String, String> form) {
        Map<String, String> map = new HashMap<>();
        form.forEach((key, value) -> map.put(key, value.get(0)));

        System.out.println("=== PAYMENT NOTIFICATION RECEIVED ===");
        System.out.println("Raw form data: " + form);
        System.out.println("Processed map: " + map);

        // Validate that we have the minimum required fields
        if (!map.containsKey("merchant_id") || !map.containsKey("order_id") ||
            !map.containsKey("status_code") || !map.containsKey("md5sig")) {
            System.out.println("Missing required fields in payment notification");
            return "Payment FAILED or INVALID - Missing required fields";
        }

        boolean success = paymentService.processNotification(map);
        String result = success ? "Payment SUCCESS" : "Payment FAILED or INVALID";

        System.out.println("=== PAYMENT NOTIFICATION RESULT ===");
        System.out.println("Processing result: " + result);
        System.out.println("Success: " + success);

        return result;
    }
}
