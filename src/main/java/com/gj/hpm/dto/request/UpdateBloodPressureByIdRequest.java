package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class UpdateBloodPressureByIdRequest extends BaseRequest {
    @NotBlank(message = "{notEmpty.message}")
    private String userId;
    @NotBlank(message = "{notEmpty.message}")
    private String bloodPressureId;
    @NotBlank(message = "{notEmpty.message}")
    private String sys;
    @NotBlank(message = "{notEmpty.message}")
    private String dia;
    @NotBlank(message = "{notEmpty.message}")
    private String pul;
}
