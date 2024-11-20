package com.example.showtime.transaction.model.response;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SSLRefundResponse {
    @SerializedName("APIConnect")
    private String APIConnect;

    @SerializedName("bank_tran_id")
    private String bankTranId;

    @SerializedName("trans_id")
    private String transId;

    @SerializedName("refund_ref_id")
    private String refundRefId;

    private String status;
    private String errorReason;
}
