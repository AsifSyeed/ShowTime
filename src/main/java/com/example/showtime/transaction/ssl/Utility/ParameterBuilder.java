package com.example.showtime.transaction.ssl.Utility;

import com.example.showtime.user.model.entity.UserAccount;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class ParameterBuilder {

    public static String getParamsString(Map<String, String> params, boolean urlEncode) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (urlEncode)
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            else
                result.append(entry.getKey());

            result.append("=");
            if (urlEncode)
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            else
                result.append(entry.getValue());
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public static Map<String, String> constructRequestParameters(String transactionReferenceNo, Double totalPrice, UserAccount createdBy, String redirectBaseUrl) {

        // CREATING LIST OF POST DATA
        Map<String, String> postData = new HashMap<String, String>();
        postData.put("total_amount", totalPrice.toString());
        postData.put("tran_id", transactionReferenceNo); // use unique tran_id for each API call
        postData.put("success_url", redirectBaseUrl + "/api/v1/transaction/ssl-redirect");
        postData.put("fail_url", redirectBaseUrl + "/api/v1/transaction/ssl-redirect");
        postData.put("cancel_url", redirectBaseUrl + "/api/v1/transaction/ssl-redirect");
        postData.put("cus_name", createdBy.getFirstName() + createdBy.getLastName());
        postData.put("cus_email", createdBy.getEmail());
        postData.put("cus_add1", "Address Line One");
        postData.put("cus_city", "Dhaka");
        postData.put("cus_postcode", "1000");
        postData.put("cus_country", "Bangladesh");
        postData.put("cus_phone", createdBy.getPhoneNumber());
        postData.put("shipping_method", "NO");
        postData.put("product_name", "Test Product");
        postData.put("product_category", "General");
        postData.put("product_profile", "General");
//        postData.put("ship_name", "ABC XY");
//        postData.put("ship_add1", "Address Line One");
//        postData.put("ship_add2", "Address Line Two");
//        postData.put("ship_city", "City Name");
//        postData.put("ship_state", "State Name");
//        postData.put("ship_postcode", "Post Code");
//        postData.put("ship_country", "Country");
        return postData;
    }
}
