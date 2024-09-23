package com.example.showtime.referral.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "REFERRAL")
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "REFERRAL_CODE", unique = true)
    private String referralCode;

    @Column(name = "REFERRAL_DISCOUNT")
    private Double referralDiscount;

    @Column(name = "REFERRAL_CREATED_BY")
    private String referralCreatedBy;

    @Column(name = "REFERRAL_CREATED_DATE")
    private Date referralCreatedDate;

    @Column(name = "EVENT_ID")
    private String eventId;

    @Column(name = "REFERRAL_TYPE")
    private Integer referralType;
}
