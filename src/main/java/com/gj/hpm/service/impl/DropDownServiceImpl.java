package com.gj.hpm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gj.hpm.dto.response.GetUserListResponse;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.service.DropDownService;

@Service
public class DropDownServiceImpl implements DropDownService {
    @Autowired
    private StmUserRepository stmUserRepository;

    @Override
    public List<GetUserListResponse> getUserList() {
        return stmUserRepository.findAllUserWithLineId();
    }
}
