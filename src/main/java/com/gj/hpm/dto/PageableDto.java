package com.gj.hpm.dto;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.BaseStatusResponse;
import com.gj.hpm.util.Constant.ApiReturn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageableDto<T> extends BaseResponse{
    private List<T> content;
    private long totalItems;
    private int totalPages;
    private int pageNo;
    private int pageSize;
    private String sortColumn;
    private String sortDirection;


    public PageableDto(Page<T> content) {
        // super(b);
        this.setContent(content.getContent());
        this.setPageNo(content.getNumber());
        this.setPageSize(content.getSize());
        this.setTotalItems(content.getTotalElements());
        this.setTotalPages(content.getTotalPages());
        this.setStatus(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
        ApiReturn.SUCCESS.description(), null)); 
    }


    public PageableDto(List<T> content , Pageable pageable, long total) {
        // super(b);
        this.setContent(content);
        this.setPageNo(pageable.getPageNumber());
        this.setPageSize(pageable.getPageSize());
        this.setTotalItems(total);
        this.setTotalPages(Long.valueOf(total / pageable.getPageSize() ).intValue());
        this.setStatus(new BaseStatusResponse(ApiReturn.SUCCESS.code(),
        ApiReturn.SUCCESS.description(), null)); 
    }


    
    

}
