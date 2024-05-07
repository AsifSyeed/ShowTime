package com.example.showtime.s3.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${application.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    public String uploadFile(MultipartFile file) {
        File fileObject = convertMultipartToFile(file);
        String fileName = "generic_ticket";
        s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObject));
        fileObject.delete();

        return "File uploaded successfully: " + fileName;
    }

    public byte[] downloadFile(String fileName) {
        S3Object s3Object = s3Client.getObject(bucketName, fileName);
        S3ObjectInputStream stream = s3Object.getObjectContent();

        try {
            return IOUtils.toByteArray(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteFile(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
        return "File deleted successfully: " + fileName;
    }

    private File convertMultipartToFile(MultipartFile file) {
        File convertedFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert file to multipart", e);
        }
        return convertedFile;
    }
}
