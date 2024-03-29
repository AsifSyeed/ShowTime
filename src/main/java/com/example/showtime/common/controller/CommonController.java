package com.example.showtime.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "https://www.countersbd.com"}, maxAge = 3600)
@RestController
@RequiredArgsConstructor
public class CommonController {
    @GetMapping("/")
    public ResponseEntity<String> getBaseResponse() {
        String title = "Welcome to Counters BD!";
        String subtitle = "Service is running";
        String response = String.format("%s\n%s", title, subtitle);
        return ResponseEntity.status(HttpStatus.OK.value()).body(response);
    }
}