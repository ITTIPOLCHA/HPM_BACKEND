package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class DeleteBloodPressureByIdRequest extends BaseRequest {
    @NotBlank(message = "{notEmpty.message}")
    private String userId;
    @NotBlank(message = "{notEmpty.message}")
    private String bloodPressureId;
}
