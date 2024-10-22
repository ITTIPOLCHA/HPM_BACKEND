package com.gj.hpm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class BasePaginationResponse extends BaseResponse {
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private int size;
    private int number;
    private boolean empty;

}
