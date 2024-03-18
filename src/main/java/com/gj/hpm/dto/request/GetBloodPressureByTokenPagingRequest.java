package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class GetBloodPressureByTokenPagingRequest extends BasePaginationRequest {
    private String sys;
    private String dia;
    private String pul;
    private String createBy;
    private String statusFlag;
}
