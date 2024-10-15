package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class GetBloodPressurePagingRequest extends BasePaginationRequest {
    private int systolicPressure;
    private int diastolicPressure;
    private int pulseRate;
    private String createBy;
    private String statusFlag;
}
