package com.gj.hpm.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntity implements Serializable {
    @Id
    private String id;
    private String statusFlag;
    
    @DBRef
    private User createBy;
    @CreatedDate
    private LocalDateTime createDate;

    @DBRef
    private User updateBy;
    @LastModifiedDate
    private LocalDateTime updateDate;

}
