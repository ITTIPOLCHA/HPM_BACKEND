package com.gj.hpm.service;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetUserByIdResp;
import com.gj.hpm.dto.response.GetUserByTokenResp;
import com.gj.hpm.dto.response.GetUserPagingResponse;

public interface UserService {
    GetUserByIdResp getUserById(GetUserByIdRequest request);

    GetUserByTokenResp getUserByToken(String request);

    GetUserPagingResponse getUserPaging(GetUserPagingRequest request);

    BaseResponse updateUserById(BaseRequest request);

    BaseResponse deleteUserById(BaseRequest request);
}
