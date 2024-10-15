package com.gj.hpm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gj.hpm.dto.response.GetStateResponse;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.repository.BloodPressureRecordRepository;
import com.gj.hpm.service.StateService;
import com.gj.hpm.util.Constant.Level;
import com.gj.hpm.util.Constant.StatusFlag;

@Service
@Transactional
public class StateServiceImpl implements StateService {
        @Autowired
        private StmUserRepository stmUserRepository;
        @Autowired
        private BloodPressureRecordRepository stpBloodPressureRepository;

        @Override
        @Transactional(readOnly = true)
        public GetStateResponse getState() {
                List<User> users = stmUserRepository.findAllUserWithLineIdInState();
                GetStateResponse response = new GetStateResponse();

                long userSentCount = users.stream()
                                .filter(u -> StatusFlag.ACTIVE.toString().equals(u.getStatusFlag()))
                                .count();
                long userUnsentCount = users.stream()
                                .filter(u -> StatusFlag.INACTIVE.toString().equals(u.getStatusFlag()))
                                .count();
                long userWarningCount = users.stream()
                                .filter(u -> !Level.NORMAL.toString().equals(u.getLevel()))
                                .count();

                response.setUserAll(users.size());
                response.setUserSent((int) userSentCount);
                response.setUserUnsent((int) userUnsentCount);
                response.setUserWarning((int) userWarningCount);

                if (response.getUserAll() != 0) {
                        response.setPercentUserSent(response.getUserSent() * 100 / response.getUserAll());
                        response.setPercentUserUnsent(response.getUserUnsent() * 100 / response.getUserAll());
                        response.setPercentUserWarning(response.getUserWarning() * 100 / response.getUserAll());
                } else {
                        response.setPercentUserSent(0);
                        response.setPercentUserUnsent(0);
                        response.setPercentUserWarning(0);
                }

                response.setBloodPressureCurrent(stpBloodPressureRepository.findByCurrent().orElse(null));

                return response;
        }
}
