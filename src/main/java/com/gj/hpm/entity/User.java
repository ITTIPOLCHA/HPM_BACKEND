package com.gj.hpm.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class User extends BaseEntity {
    private String email;
    private String username;
    private String password;
    private String hn;
    private String phone;
    private String firstName;
    private String lastName;
    // get from line token
    private String lineId;
    private String lineName;
    private byte[] picture;
    @DBRef
    private Set<Role> roles = new HashSet<>();

}
