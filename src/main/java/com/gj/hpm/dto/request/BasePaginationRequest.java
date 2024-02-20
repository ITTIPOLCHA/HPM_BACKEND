package com.gj.hpm.dto.request;

import java.util.List;

import com.gj.hpm.dto.SortOrder;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BasePaginationRequest extends BaseRequest {
  private int page;
  private int size;
  private List<SortOrder> sortBy; // รับค่าการเรียงลำดับ

  /*
   * {
   * "page": 0,
   * "size": 10,
   * "sortBy": [
   * {"direction": "ASC", "property": "orderName"},
   * {"direction": "DESC", "property": "createDate"}
   * ]
   * }
   */

}
