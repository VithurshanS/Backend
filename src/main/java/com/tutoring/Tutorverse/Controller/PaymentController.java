package com.tutoring.Tutorverse.Controller;



import com.tutoring.Tutorverse.Dto.PaymentDto;
import com.tutoring.Tutorverse.Services.PaymentService;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    public Map<String, Object> createPayment(@RequestBody PaymentDto req) {
        return paymentService.createPayment(req.getStudentId(), req.getModuleId(), req.getAmount());
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
