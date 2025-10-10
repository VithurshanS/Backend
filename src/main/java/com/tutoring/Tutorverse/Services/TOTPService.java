package com.tutoring.Tutorverse.Services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TOTPService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @Value("${totp.issuer:${spring.application.name:Tutorverse}}")
    private String issuer;

    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public String getQRCode(String secretKey, String username) throws WriterException, IOException {
        String encIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encUser = URLEncoder.encode(username, StandardCharsets.UTF_8);
        String label = encIssuer + ":" + encUser;
        String otpAuthURL = String.format(
                "otpauth://totp/%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                label, secretKey, encIssuer
        );

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthURL, BarcodeFormat.QR_CODE, 300, 300);

        ByteArrayOutputStream pngOutput = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutput);

        return Base64.getEncoder().encodeToString(pngOutput.toByteArray());
    }

    public boolean verifyCode(String secretKey, int code) {
        return gAuth.authorize(secretKey, code);
    }
}
