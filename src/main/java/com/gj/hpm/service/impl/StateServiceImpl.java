package com.gj.hpm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gj.hpm.dto.response.GetStateResponse;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.repository.StpBloodPressureRepository;
import com.gj.hpm.service.StateService;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;

@Service
public class StateServiceImpl implements StateService {
        @Autowired
        private StmUserRepository stmUserRepository;
        @Autowired
        private StpBloodPressureRepository stpBloodPressureRepository;

        @Override
        public GetStateResponse getState() {
                List<User> users = stmUserRepository.findAllUserWithLineIdInState();
                GetStateResponse response = new GetStateResponse();

                response.setUserAll(users.size());
                response.setUserSent(
                                (int) users.stream().filter(u -> u.getStatusFlag().equals(StatusFlag.ACTIVE.toString()))
                                                .count());
                response.setUserUnsent(
                                (int) users.stream()
                                                .filter(u -> u.getStatusFlag().equals(StatusFlag.INACTIVE.toString()))
                                                .count());
                response.setUserWarning(
                                (int) users.stream().filter(u -> !u.getLevel().equals(Level.NORMAL.toString()))
                                                .count());
                response.setPercentUserSent(response.getUserSent() * 100 / response.getUserAll());
                response.setPercentUserUnsent(response.getUserUnsent() * 100 / response.getUserAll());
                response.setPercentUserWarning(response.getUserWarning() * 100 / response.getUserAll());
                response.setBloodPressureCurrent(stpBloodPressureRepository.findByCurrent());

                return response;
        }
}
