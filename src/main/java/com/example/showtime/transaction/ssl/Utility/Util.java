package com.example.showtime.transaction.ssl.Utility;

import com.example.showtime.transaction.model.response.SSLRefundResponse;
import com.example.showtime.transaction.ssl.parametermappings.SSLCommerzInitResponse;
import com.example.showtime.transaction.ssl.parametermappings.SSLCommerzValidatorResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Util {

    public static SSLCommerzInitResponse extractInitResponse(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SSLCommerzInitResponse sslInitResponse = mapper.readValue(response, SSLCommerzInitResponse.class);
        return sslInitResponse;
    }

    public static SSLCommerzValidatorResponse extractValidatorResponse(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SSLCommerzValidatorResponse sslValidatorResponse = mapper.readValue(response, SSLCommerzValidatorResponse.class);
        return sslValidatorResponse;
    }

    public static String getByOpeningJavaUrlConnection(String stringUrl) throws IOException {
        String output = "";
        URL url = new URL(stringUrl);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String outputLine;
        while ((outputLine = br.readLine()) != null) {
            output = output + outputLine;
        }
        br.close();
        return output;
    }

    public static SSLRefundResponse extractRefundResponse(String json) {
        Gson gson = new Gson();

        return gson.fromJson(json, SSLRefundResponse.class);
    }
}
