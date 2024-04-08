package com.gj.hpm.service;

import java.util.List;

import com.gj.hpm.dto.response.GetUserListResponse;

public interface DropDownService {
    List<GetUserListResponse> getUserList();
}
