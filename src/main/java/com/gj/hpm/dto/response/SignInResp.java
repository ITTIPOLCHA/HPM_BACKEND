package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInResp extends BaseResponse {
    private String token;
    private String email;
    private String name; // firstName + lastName
    private String lineName;
    private String picture;
    private String phone;
    private String hn;
    private String role;
}
