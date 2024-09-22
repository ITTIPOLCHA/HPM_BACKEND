package com.gj.hpm.util;

import org.springframework.http.ResponseEntity;
import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;

import java.util.Collections;
import java.util.List;

public class ResponseUtil {

    public static BaseStatusResponse buildBaseStatusResponse(String code, String description, String key,
            String value) {
        BaseDetailsResponse detailsResponse = new BaseDetailsResponse(key, value);
        return new BaseStatusResponse(code, description,
                Collections.singletonList(detailsResponse));
    }

    public static BaseResponse buildBaseResponse(String code, String description, String key, String value) {
        BaseDetailsResponse detailsResponse = new BaseDetailsResponse(key, value);
        BaseStatusResponse statusResponse = new BaseStatusResponse(code, description,
                Collections.singletonList(detailsResponse));
        return new BaseResponse(statusResponse);
    }

    public static BaseResponse buildListBaseResponse(String code, String description, List<BaseDetailsResponse> value) {
        BaseStatusResponse statusResponse = new BaseStatusResponse(code, description, value);
        return new BaseResponse(statusResponse);
    }

    public static ResponseEntity<BaseResponse> buildSuccessResponse(String code, String description, String key,
            String value) {
        BaseDetailsResponse detailsResponse = new BaseDetailsResponse(key, value);
        BaseStatusResponse statusResponse = new BaseStatusResponse(code, description,
                Collections.singletonList(detailsResponse));
        return ResponseEntity.ok().body(new BaseResponse(statusResponse));
    }

    public static ResponseEntity<BaseResponse> buildErrorResponse(String code, String description, String key,
            String value) {
        BaseDetailsResponse detailsResponse = new BaseDetailsResponse(key, value);
        BaseStatusResponse statusResponse = new BaseStatusResponse(code, description,
                Collections.singletonList(detailsResponse));
        return ResponseEntity.badRequest().body(new BaseResponse(statusResponse));
    }

}
