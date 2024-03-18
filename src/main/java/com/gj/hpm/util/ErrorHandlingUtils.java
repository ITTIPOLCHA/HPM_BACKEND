package com.gj.hpm.util;

import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import com.gj.hpm.dto.response.BaseDetailsResponse;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.util.Constant.ApiReturn;

public class ErrorHandlingUtils {

    public static ResponseEntity<?> handleBindingErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            ArrayList<BaseDetailsResponse> detail = new ArrayList<>();
            bindingResult.getFieldErrors().forEach(error -> {
                detail.add(new BaseDetailsResponse(error.getField(), error.getDefaultMessage()));
            });
            return ResponseEntity.badRequest().body(
                    new BaseResponse(new BaseStatusResponse(ApiReturn.BAD_REQUEST.code(),
                            ApiReturn.BAD_REQUEST.description(), detail)));
        }
        return null; // No errors, return null or an appropriate indicator
    }
}
