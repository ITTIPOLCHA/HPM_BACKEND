package com.gj.hpm.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetBloodPressurePagingResponse extends BasePaginationResponse {
    List<GetBloodPressureDetailPagingResponse> content;
}
