package com.gj.hpm.service;

import java.util.List;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.CreateUserRequest;
import com.gj.hpm.dto.request.GetAdminPagingRequest;
import com.gj.hpm.dto.request.GetUserByIdRequest;
import com.gj.hpm.dto.request.GetUserPagingRequest;
import com.gj.hpm.dto.request.PasswordChangeRequest;
import com.gj.hpm.dto.request.PasswordForgotRequest;
import com.gj.hpm.dto.request.SignInRequest;
import com.gj.hpm.dto.request.SignUpRequest;
import com.gj.hpm.dto.request.UpdateUserByIdRequest;
import com.gj.hpm.dto.request.UpdateUserByTokenRequest;
import com.gj.hpm.dto.request.UpdateUserCheckStateRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetUserListByLevelResponse;
import com.gj.hpm.dto.response.GetUserListByStatusFlagResponse;
import com.gj.hpm.dto.response.GetUserResponse;
import com.gj.hpm.dto.response.JwtResponse;

public interface UserService {
    BaseResponse setInactive();

    BaseResponse sentMultiMessage();

    JwtResponse signIn(SignInRequest request);

    BaseResponse signUp(SignUpRequest request);

    BaseResponse createUser(CreateUserRequest request);

    BaseResponse changePassword(String token, PasswordChangeRequest request);

    BaseResponse forgotPassword(PasswordForgotRequest request);

    GetUserResponse getUserById(GetUserByIdRequest request);

    GetUserResponse getUserByToken(String id);

    BaseResponse getUserPaging(GetUserPagingRequest request);

    BaseResponse getAdminPaging(GetAdminPagingRequest request);

    List<GetUserListByLevelResponse> getUserListByLevel();

    List<GetUserListByStatusFlagResponse> getUserListByStatusFlag();

    BaseResponse updateUserById(String id, UpdateUserByIdRequest request);

    BaseResponse updateUserByToken(String id, UpdateUserByTokenRequest request);

    BaseResponse updateUserCheckState(UpdateUserCheckStateRequest request);

    BaseResponse deleteUserById(GetUserByIdRequest request);

    BaseResponse deleteUserByToken(String id, BaseRequest request);
}
