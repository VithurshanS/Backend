package com.tutoring.Tutorverse.SecurityConfigs;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;

public class PayHereHash {

    public static String md5Upper(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(s.getBytes());
            BigInteger no = new BigInteger(1, digest);
            String hex = no.toString(16);
            while (hex.length() < 32) hex = "0" + hex;
            return hex.toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String formatAmount(double amount) {
        return new DecimalFormat("0.00").format(amount).replace(",", "");
    }

    public static String buildHash(String merchantId, String orderId, double amount, String currency, String merchantSecret) {
        String amountFormatted = formatAmount(amount);
        String hashedSecret = md5Upper(merchantSecret);
        return md5Upper(merchantId + orderId + amountFormatted + currency + hashedSecret);
    }

    public static String buildNotifyHash(String merchantId, String orderId, String payhereAmount,
                                         String payhereCurrency, String statusCode, String merchantSecret) {
        String hashedSecret = md5Upper(merchantSecret);
        return md5Upper(merchantId + orderId + payhereAmount + payhereCurrency + statusCode + hashedSecret);
    }

}

