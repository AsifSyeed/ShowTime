package com.example.showtime.refund.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "REFUND_ITEM")
public class RefundItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TRANSACTION_REF_NO")
    private String transactionRefNo;

    @Column(name = "EVENT_ID")
    private String eventId;

    @Column(name = "USER_EMAIL")
    private String userEmail;

    @Column(name = "REFUND_AMOUNT")
    private Double refundAmount;

    @Column(name = "REFUND_DATE")
    private Date refundDate;

    @Column(name = "REFUND_STATUS")
    private int refundStatus;

    @Column(name = "BANK_TRAN_ID")
    private String bankTranId;

    @Column(name = "REFUND_REF_ID")
    private String refundRefId;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "REMARKS")
    private String remarks;
}
