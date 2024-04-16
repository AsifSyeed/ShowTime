package com.example.showtime.transaction.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "TRANSACTION_ITEM")
public class TransactionItem {
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

    @Column(name = "TOTAL_AMOUNT")
    private Double totalAmount;

    @Column(name = "TRANSACTION_DATE")
    private Date transactionDate;

    @Column(name = "TRANSACTION_STATUS")
    private int transactionStatus;
}
