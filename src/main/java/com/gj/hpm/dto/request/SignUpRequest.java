package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import com.gj.hpm.annotation.MobilePhone;
import com.gj.hpm.annotation.Password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Validated
public class SignUpRequest extends BaseRequest {
    @NotEmpty(message = "{notEmpty.message}")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "{msg.signup.err.valid.email}")
    private String email;
    @NotEmpty(message = "{notEmpty.message}")
    @Password
    private String password;
    @NotEmpty(message = "{notEmpty.message}")
    private String hospitalNumber;
    @NotEmpty(message = "{notEmpty.message}")
    @MobilePhone
    private String phoneNumber;
    @NotEmpty(message = "{notEmpty.message}")
    private String firstName;
    @NotEmpty(message = "{notEmpty.message}")
    private String lastName;
    private String gender;
    private int age;
    // line token
    private String lineToken;
}
