package com.gj.hpm.service;

import java.util.List;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetUserListByLevelResponse;
import com.gj.hpm.dto.response.GetUserListByStatusFlagResponse;
import com.gj.hpm.dto.response.GetUserPagingResponse;
import com.gj.hpm.dto.response.GetUserResponse;

public interface UserService {
    GetUserResponse getUserById(GetUserByIdRequest request);

    GetUserResponse getUserByToken(String id);

    GetUserPagingResponse getUserPaging(GetUserPagingRequest request);

    List<GetUserListByLevelResponse> getUserListByLevel();

    List<GetUserListByStatusFlagResponse> getUserListByStatusFlag();

    BaseResponse updateUserById(String id, UpdateUserByIdRequest request);

    BaseResponse updateUserByToken(String id, UpdateUserByTokenRequest request);

    BaseResponse deleteUserById(GetUserByIdRequest request);

    BaseResponse deleteUserByToken(String id, BaseRequest request);
}
