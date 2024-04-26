package com.gj.hpm.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserResponse extends BaseResponse {
    private String id;
    private String email;
    private String hn;
    private String phone;
    private String firstName;
    private String lastName;
    private String lineName;
    private String pictureUrl;
    private String statusFlag;
    private String level;
    private boolean checkState;
    private DropdownUserResp createBy;
    private LocalDateTime createDate;
    private DropdownUserResp updateBy;
    private LocalDateTime updateDate;
    private List<DropdownRoleResp> role;

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

    /**
     * InnerRole
     */
    @Setter
    @Getter
    public class DropdownRoleResp {
        private String name;
    }
}
