package com.gj.hpm.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserByIdResp extends BaseResponse {
    private String id;
    private String email;
    private String hn;
    private String phone;
    private String firstName;
    private String lastName;
    private String lineName;
    private String pictureUrl;
    private String statusFlag;
    private String createBy;
    private LocalDateTime createDate;
    private String updateBy;
    private LocalDateTime updateDate;
}
