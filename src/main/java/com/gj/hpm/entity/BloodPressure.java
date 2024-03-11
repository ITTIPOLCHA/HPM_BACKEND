package com.gj.hpm.entity;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class BloodPressure extends BaseEntity{
    private String sys;
    private String dia;
    private String pul;
}
