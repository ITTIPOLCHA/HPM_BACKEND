package com.gj.hpm.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBloodPressureOnPhotoResponse extends BaseResponse {
    private int sys;
    private int dia;
    private int pul;
}
