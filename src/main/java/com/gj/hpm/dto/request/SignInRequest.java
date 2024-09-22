package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import com.gj.hpm.annotation.FixedValue;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Validated
public class SignInRequest extends BaseRequest {
    @NotBlank(message = "{notEmpty.message}")
    @FixedValue
    private String type;
    // @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message =
    // "{msg.signup.err.valid.email}")
    private String email;
    private String password;
    private String lineToken;
}
