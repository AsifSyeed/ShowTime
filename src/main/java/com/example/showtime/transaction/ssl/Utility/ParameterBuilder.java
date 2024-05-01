package com.example.showtime.transaction.ssl.Utility;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ParameterBuilder {
    private static UserRepository userRepository;

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

    public static Map<String, String> constructRequestParameters(TransactionItem transactionItem, UserAccount createdBy) {

        // CREATING LIST OF POST DATA
        String baseUrl = "https://api.countersbd.com/";//Request.Url.Scheme + "://" + Request.Url.Authority + Request.ApplicationPath.TrimEnd('/') + "/";
        Map<String, String> postData = new HashMap<String, String>();
        postData.put("total_amount", transactionItem.getTotalAmount().toString());
        postData.put("tran_id", transactionItem.getTransactionRefNo()); // use unique tran_id for each API call
        postData.put("success_url", baseUrl + "api/v1/transaction/ssl-redirect");
        postData.put("fail_url", baseUrl + "api/v1/transaction/ssl-redirect");
        postData.put("cancel_url", baseUrl + "api/v1/transaction/ssl-redirect");
        postData.put("cus_name", createdBy.getUserFullName());
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
        postData.put("ipn_url", "https://api.countersbd.com/api/v1/transaction/ssl-redirect");
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
