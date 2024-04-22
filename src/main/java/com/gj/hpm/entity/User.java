package com.gj.hpm.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Document
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
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
    private String lineSubId;
    private String lineName;
    private String pictureUrl;
    // status
    private String level; // warning1, warning2, danger, safe
    private boolean checkState; // active, inactive, delete
    @DBRef
    private Set<Role> roles = new HashSet<>();

}
