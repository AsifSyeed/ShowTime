package com.example.showtime.transaction.ssl;

import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
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

    public String initiateSSLTransaction(TransactionItem transactionItem, UserAccount createdBy) {
        String response = "";
        try {
            /**
             * All parameters in payment order should be constructed in this follwing postData Map
             * keep an eye on success fail url correctly.
             * insert your success and fail URL correctly in this Map
             */
            Map<String, String> postData = ParameterBuilder.constructRequestParameters(transactionItem, createdBy);
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

    public boolean verifySSLTransaction(CheckTransactionStatusRequest checkTransactionStatusRequest) {
        boolean response = false;
        try {
            SSLCommerz sslcz = new SSLCommerz(storeId, storePassword, storeTestMode);
            response = sslcz.orderValidate(checkTransactionStatusRequest.getTransactionRefNo(), checkTransactionStatusRequest.getTransactionAmount(), checkTransactionStatusRequest.getTransactionCurrency(), checkTransactionStatusRequest.getValidationId());
            return response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
}
