package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class UpdateUserCheckStateRequest extends BaseRequest {
    @NotBlank(message = "{notEmpty.message}")
    private String patientId;
    private boolean isVerified;
}
