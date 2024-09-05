package com.gj.hpm.entity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.gj.hpm.util.Constant.Level;

import lombok.AllArgsConstructor;
import lombok.Builder;
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

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String username;

    private String password;
    private String hn;
    private String phone;
    private String firstName;
    private String lastName;

    private String lineId;
    private String lineSubId;
    private String lineName;

    private String pictureUrl;

    private Level level;
    private boolean checkState;

    @Builder.Default
    @DBRef
    private Set<Role> roles = new HashSet<>();
}
