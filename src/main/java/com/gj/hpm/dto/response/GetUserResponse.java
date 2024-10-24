package com.gj.hpm.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.gj.hpm.util.Constant.Level;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserResponse extends BaseResponse {
    private String id;
    private String email;
    private String hospitalNumber;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String lineName;
    private String pictureUrl;
    private String statusFlag;
    private Level level;
    private boolean verified;
    private DropdownUserResp createBy;
    private LocalDateTime createDate;
    private DropdownUserResp updateBy;
    private LocalDateTime updateDate;
    private List<DropdownRoleResp> roles;

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
