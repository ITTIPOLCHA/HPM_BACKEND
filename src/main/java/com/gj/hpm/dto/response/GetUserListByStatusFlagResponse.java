package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserListByStatusFlagResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String hn;
    private String phone;
    private String statusFlag;
}
