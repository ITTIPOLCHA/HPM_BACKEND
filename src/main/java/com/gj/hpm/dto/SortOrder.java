package com.gj.hpm.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SortOrder {
    private String direction; // "ASC: DESC"
    private String property;

}
