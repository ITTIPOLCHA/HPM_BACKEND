package com.gj.hpm.dto.request;

import org.springframework.validation.annotation.Validated;

import com.gj.hpm.annotation.MobilePhone;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
public class GetUserPagingRequest extends BasePaginationRequest {
    private String firstName;
    private String lastName;
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", message = "{msg.signup.err.valid.email}")
    private String email;
    @MobilePhone
    private String phoneNumber;
    private String hospitalNumber;
    private String statusFlag;
    private String level;
    private String gender;
    private Integer ageFrom;
    private Integer ageTo;
}
