package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBloodPressureCurrentResponse {
    private String systolicPressure;
    private String diastolicPressure;
    private String pulseRate;
    private DropdownUserResp createBy;

    /**
     * InnerGeneralInformation
     */
    @Setter
    @Getter
    public class DropdownUserResp {
        private String firstName;
        private String lastName;
        private String hn;
    }
}
