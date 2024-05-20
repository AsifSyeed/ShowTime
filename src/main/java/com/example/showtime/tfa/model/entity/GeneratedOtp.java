package com.example.showtime.tfa.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "GENERATED_OTP")
public class GeneratedOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_AT")
    private Date createdAt;

    @Column(name = "EXPIRE_AT")
    private Date expireAt;

    @Column(name = "OTP")
    private String otp;

    @Column(name = "FEATURE_CODE")
    private Integer featureCode;

    @Column(name = "SESSION_ID")
    private String sessionId;

    @Column(name = "IS_USED")
    private Boolean isUsed;
}
