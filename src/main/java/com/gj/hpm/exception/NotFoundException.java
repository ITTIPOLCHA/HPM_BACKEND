package com.gj.hpm.exception;

import com.gj.hpm.dto.response.BaseResponse;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final BaseResponse response;

    public NotFoundException(BaseResponse response) {
        this.response = response;
    }
}