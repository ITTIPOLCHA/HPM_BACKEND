package com.gj.hpm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.dto.request.GetUserListRequest;
import com.gj.hpm.dto.response.GetUserListResponse;
import com.gj.hpm.service.DropDownService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/dropdown")
public class DropDownController {
    @Autowired
    private DropDownService dropDownService;

    // ! G
    @PostMapping("/getUserList")
    public ResponseEntity<?> getUserList(@RequestHeader("Authorization") String token,
            @RequestBody GetUserListRequest request) {
        try {
            List<GetUserListResponse> response = dropDownService.getUserList();
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
