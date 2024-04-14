package com.gj.hpm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.response.GetStateResponse;
import com.gj.hpm.service.StateService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/state")
public class StateController {
    @Autowired
    private StateService stateService;

    // ! R
    @PostMapping("/getState")
    public ResponseEntity<?> getState(@RequestHeader("Authorization") String token,
            @RequestBody BaseRequest request) {
        try {
            GetStateResponse response = stateService.getState();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
