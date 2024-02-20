package com.gj.hpm.controller;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gj.hpm.config.security.jwt.JwtUtils;
import com.gj.hpm.config.security.services.UserDetailsImpl;
import com.gj.hpm.dto.request.SignInReq;
import com.gj.hpm.dto.request.SignUpReq;
import com.gj.hpm.dto.response.BaseDetailsResp;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResp;
import com.gj.hpm.dto.response.JwtResp;
import com.gj.hpm.entity.ERole;
import com.gj.hpm.entity.Role;
import com.gj.hpm.entity.User;
import com.gj.hpm.repository.StmRoleRepository;
import com.gj.hpm.repository.StmUserRepository;
import com.gj.hpm.util.Constant.ApiReturn;
import com.gj.hpm.util.Constant.Key;
import com.gj.hpm.util.Constant.StatusFlag;
import com.gj.hpm.util.Constant.TypeSignIn;
import com.nimbusds.jwt.JWTClaimsSet;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/v1/system")
public class SystemController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    StmUserRepository userRepository;

    @Autowired
    StmRoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInReq req) {
        try {
            Authentication authentication = null;
            if (TypeSignIn.line.toString().equals(req.getType())) {
                JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(req.getLineToken());
                String lineId = claimsSet.getSubject();
                User user = userRepository.findByLineId(lineId).orElse(null);
                if (user == null)
                    return ResponseEntity
                            .badRequest()
                            .body("Error: User not found!");
            } else {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new JwtResp(jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    @PostMapping("/signUp")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpReq req) {
        try {
            List<BaseDetailsResp> details = new ArrayList<>();
            if (userRepository.existsByEmail(req.getEmail())) {
                details.add(new BaseDetailsResp("Fail ❎", "Error: Email is already in use!"));
                return ResponseEntity.badRequest().body(new BaseResponse(
                        new BaseStatusResp(ApiReturn.BAD_REQUEST.code(), ApiReturn.BAD_REQUEST.description(),
                                details)));
            }
            // Create new user's account
            User user = new User();
            BeanUtils.copyProperties(req, user);
            user.setUsername(req.getEmail());
            user.setPassword(encoder.encode(req.getPassword()));
            Set<Role> roles = new HashSet<>();
            Role role = new Role();
            if (StringUtils.isBlank(req.getLineToken())) {
                role = roleRepository.findByName(ERole.ROLE_ADMIN);
            } else {
                role = roleRepository.findByName(ERole.ROLE_USER);
                JWTClaimsSet claimsSet = jwtUtils.decodeES256Jwt(req.getLineToken());
                String name = claimsSet.getClaim(Key.name.toString()).toString();
                String imageUrl = claimsSet.getClaim(Key.picture.toString()).toString();
                URL url = new URL(imageUrl);
                byte[] imageBytes = url.openStream().readAllBytes();
                user.setLineId(claimsSet.getSubject());
                user.setLineName(name);
                user.setPicture(imageBytes);
            }
            roles.add(role);
            user.setRoles(roles);
            userRepository.save(user);
            LocalDateTime now = LocalDateTime.now();
            user.setCreateBy(user.getId());
            user.setUpdateBy(user.getId());
            user.setCreateDate(now);
            user.setUpdateDate(now);
            user.setStatusFlag(StatusFlag.ACTIVE.code());
            userRepository.save(user);
            details.add(new BaseDetailsResp("Success ✅", "User registered successfully!"));
            return ResponseEntity.ok(new BaseResponse(
                    new BaseStatusResp(ApiReturn.SUCCESS.code(), ApiReturn.SUCCESS.description(), details)));
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
