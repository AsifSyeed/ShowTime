package com.example.showtime.admin.model.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity(name = "ADMIN")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ADMIN_NAME")
    private String adminName;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "ADMIN_ROLE")
    private int role;

    @Column(name = "ADMIN_CREATION_DATE")
    private Date adminCreationDate;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "CREATED_BY")
    private String createdBy;
}
