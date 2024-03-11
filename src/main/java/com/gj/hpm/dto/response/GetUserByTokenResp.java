package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserByTokenResp extends BaseResponse{
    private String email;
    private String hn;
    private String phone;
    private String firstName;
    private String lastName;
    private String lineName;
    private String pictureUrl;
}
