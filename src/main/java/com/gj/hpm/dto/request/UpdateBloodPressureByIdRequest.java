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
    private int systolicPressure;
    @NotBlank(message = "{notEmpty.message}")
    private int diastolicPressure;
    @NotBlank(message = "{notEmpty.message}")
    private int pulseRate;
}
