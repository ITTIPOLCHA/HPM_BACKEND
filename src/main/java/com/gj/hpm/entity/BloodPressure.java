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
public class BloodPressure extends BaseEntity{
    // blood pressure
    private String sys;
    private String dia;
    private String pul;

    // own
    @DBRef
    private User user;
}
