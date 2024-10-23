package com.gj.hpm.dto.request;

import com.gj.hpm.annotation.MobilePhone;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordForgotRequest extends BaseRequest {
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "{msg.signup.err.valid.email}")
    private String email;
    @MobilePhone
    private String phoneNumber;
    private String newPassword;

}
