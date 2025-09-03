package com.tutoring.Tutorverse.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Paths;
import java.io.InputStream;

@Service
public class S3Service {

    private final S3Client s3Client;
    @Value("${aws.region}")
    private String region;



    @Value("${aws.bucketName}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(String filePath, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.putObject(request, Paths.get(filePath));

        // Generate the public URL
        String url =  "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
        return url;
    }


    public InputStream downloadFile(String keyName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(keyName)
                .build();

        return s3Client.getObject(getObjectRequest);
    }
}
