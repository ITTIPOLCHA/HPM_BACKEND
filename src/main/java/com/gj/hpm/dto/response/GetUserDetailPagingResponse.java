package com.gj.hpm.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserDetailPagingResponse extends BaseResponse {
    private String id;
    private String email;
    private String hn;
    private String phone;
    private String firstName;
    private String lastName;
    private String lineName;
    private String statusFlag;
    private DropdownUserResp updateBy;
    private LocalDateTime updateDate;

    /**
     * InnerGeneralInformation
     */
    @Setter
    @Getter
    public class DropdownUserResp {
        private String id;
        private String firstName;
        private String lastName;
    }
}
