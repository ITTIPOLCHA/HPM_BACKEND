package com.gj.hpm.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserPagingResponse extends BasePaginationResponse {
    List<GetUserDetailPagingResponse> users;
}
