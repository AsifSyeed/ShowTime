package com.example.showtime.transaction.ssl;

import com.example.showtime.transaction.model.response.SSLRefundResponse;
import com.example.showtime.transaction.ssl.Utility.ParameterBuilder;
import com.example.showtime.user.model.entity.UserAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SSLTransactionInitiator {

    @Value("${sslcommerz.store.id}")
    private String storeId;

    @Value("${sslcommerz.store.password}")
    private String storePassword;

    @Value("${sslcommerz.test.mode}")
    private boolean storeTestMode;

    @Value("${sslcommerz.redirect.base.url}")
    private String redirectBaseUrl;

    public String initiateSSLTransaction(String transactionRef, Double totalPrice, UserAccount createdBy) {
        String response = "";
        try {
            /**
             * All parameters in payment order should be constructed in this follwing postData Map
             * keep an eye on success fail url correctly.
             * insert your success and fail URL correctly in this Map
             */
            Map<String, String> postData = ParameterBuilder.constructRequestParameters(transactionRef, totalPrice, createdBy, redirectBaseUrl);
            /**
             * Provide your SSL Commerz store Id and Password by this following constructor.
             * If Test Mode then insert true and false otherwise.
             */
            SSLCommerz sslcz = new SSLCommerz(storeId, storePassword, storeTestMode);

            /**
             * If user want to get Gate way list then pass isGetGatewayList parameter as true
             * If user want to get URL as returned response, pass false.
             */
            response = sslcz.initiateTransaction(postData, false);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public boolean verifySSLTransaction(String transactionRefNo, String validationId, String transactionAmount, String transactionCurrency) {
        boolean response = false;
        try {
            SSLCommerz sslcz = new SSLCommerz(storeId, storePassword, storeTestMode);
            response = sslcz.orderValidate(transactionRefNo, transactionAmount, transactionCurrency, validationId);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public SSLRefundResponse  refundTransaction(String bankTranId, String refundAmount, String refundRemarks, String refId) {
        SSLRefundResponse response = null;
        try {
            SSLCommerz sslcz = new SSLCommerz(storeId, storePassword, storeTestMode);
            response = sslcz.initiateRefund(bankTranId, refundAmount, refundRemarks, refId);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
