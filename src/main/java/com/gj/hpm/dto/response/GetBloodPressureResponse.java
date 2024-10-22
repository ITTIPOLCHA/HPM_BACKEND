package com.gj.hpm.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBloodPressureResponse extends BaseResponse {
    private String id;
    private String systolicPressure;
    private String diastolicPressure;
    private String pulseRate;
    private String statusFlag;
    private DropdownUserResp createBy;
    private LocalDateTime createDate;
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
        private String hn;
    }

}
