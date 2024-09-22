package com.gj.hpm.entity;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Document
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BloodPressureRecord extends BaseEntity {
    // blood pressure
    private String systolicPressure;
    private String diastolicPressure;
    private String pulseRate;

    // reference to the user who owns the record
    @DBRef
    private User patient;
}

