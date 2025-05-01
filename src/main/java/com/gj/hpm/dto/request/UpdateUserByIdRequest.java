package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import com.gj.hpm.annotation.MobilePhone;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class UpdateUserByIdRequest extends BaseRequest {
    @NotBlank(message = "{notEmpty.message}")
    private String userId;
    @NotBlank(message = "{notEmpty.message}")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "{msg.signup.err.valid.email}")
    private String email;
    @NotBlank(message = "{notEmpty.message}")
    private String hospitalNumber;
    @NotBlank(message = "{notEmpty.message}")
    @MobilePhone
    private String phoneNumber;
    @NotBlank(message = "{notEmpty.message}")
    private String firstName;
    @NotBlank(message = "{notEmpty.message}")
    private String lastName;
    @NotBlank(message = "{notEmpty.message}")
    private String gender;
    private int age;
}
