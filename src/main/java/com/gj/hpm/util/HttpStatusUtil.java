package com.gj.hpm.util;

import org.springframework.http.HttpStatus;

import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.util.Constant.ApiReturn;

public class HttpStatusUtil {

    public static HttpStatus determineHttpStatus(BaseResponse response) {

        if (response.getStatus().getCode().equals(ApiReturn.SUCCESS.code())) {
            return HttpStatus.OK;
        } else if (response.getStatus().getCode().equals(ApiReturn.BAD_REQUEST.code())) {
            return HttpStatus.BAD_REQUEST;
        } else if (response.getStatus().getCode().equals(ApiReturn.UNAUTHORIZED.code())) {
            return HttpStatus.UNAUTHORIZED;
        } else if (response.getStatus().getCode().equals(ApiReturn.FORBIDDEN.code())) {
            return HttpStatus.FORBIDDEN;
        } else if (response.getStatus().getCode().equals(ApiReturn.NOT_FOUND.code())) {
            return HttpStatus.NOT_FOUND;
        } else if (response.getStatus().getCode().equals(ApiReturn.INTERNAL_SERVER_ERROR.code())) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (response.getStatus().getCode().equals(ApiReturn.CREATED.code())) {
            return HttpStatus.CREATED;
        } else if (response.getStatus().getCode().equals(ApiReturn.NO_CONTENT.code())) {
            return HttpStatus.NO_CONTENT;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

}
