package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import com.gj.hpm.annotation.FixedValue;
import com.gj.hpm.annotation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Validated
public class SignInReq extends BaseRequest {
    @NotBlank(message = "{notEmpty.message}")
    @FixedValue
    private String type;
    private String email;
    private String password;
    private String lineToken;
}
