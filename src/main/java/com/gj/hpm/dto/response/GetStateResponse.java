package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetStateResponse extends BaseResponse {
    private int userAll;
    private int userSent;
    private int userUnsent;
    private int userWarning;
    private int percentUserSent;
    private int percentUserUnsent;
    private int percentUserWarning;
    private GetBloodPressureCurrentResponse bloodPressureCurrent;

}
