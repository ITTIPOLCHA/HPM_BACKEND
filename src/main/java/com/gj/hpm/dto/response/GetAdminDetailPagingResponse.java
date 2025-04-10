package com.gj.hpm.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAdminDetailPagingResponse extends BaseResponse {
    private String id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
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
