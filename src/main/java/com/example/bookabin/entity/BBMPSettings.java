package com.example.bookabin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bbmp_settings")
public class BBMPSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double collectionFee;
    private Double finePerViolation;
    private Integer bookingWindowDays;
    private String bbmpZoneName;
    private String bbmpContactEmail;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getCollectionFee() {
        return collectionFee;
    }

    public void setCollectionFee(Double collectionFee) {
        this.collectionFee = collectionFee;
    }

    public Double getFinePerViolation() {
        return finePerViolation;
    }

    public void setFinePerViolation(Double finePerViolation) {
        this.finePerViolation = finePerViolation;
    }

    public Integer getBookingWindowDays() {
        return bookingWindowDays;
    }

    public void setBookingWindowDays(Integer bookingWindowDays) {
        this.bookingWindowDays = bookingWindowDays;
    }

    public String getBbmpZoneName() {
        return bbmpZoneName;
    }

    public void setBbmpZoneName(String bbmpZoneName) {
        this.bbmpZoneName = bbmpZoneName;
    }

    public String getBbmpContactEmail() {
        return bbmpContactEmail;
    }

    public void setBbmpContactEmail(String bbmpContactEmail) {
        this.bbmpContactEmail = bbmpContactEmail;
    }
}
