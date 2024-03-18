package com.gj.hpm.service;

import com.gj.hpm.dto.request.BaseRequest;
import com.gj.hpm.dto.request.CreateBloodPressureRequest;
import com.gj.hpm.dto.request.GetBloodPressureByTokenPagingRequest;
import com.gj.hpm.dto.request.GetBloodPressurePagingRequest;
import com.gj.hpm.dto.request.GetBloodPressureRequest;
import com.gj.hpm.dto.response.BaseResponse;
import com.gj.hpm.dto.response.GetBloodPressurePagingResponse;
import com.gj.hpm.dto.response.GetBloodPressureResponse;

public interface BloodPressureService {
    BaseResponse createBloodPressure(String id, CreateBloodPressureRequest request);

    GetBloodPressureResponse getBloodPressureById(GetBloodPressureRequest request);

    GetBloodPressureResponse getBloodPressureByToken(String id, GetBloodPressureRequest request);

    GetBloodPressurePagingResponse getBloodPressurePaging(GetBloodPressurePagingRequest request);

    GetBloodPressurePagingResponse getBloodPressurePagingByUserId(String id,
            GetBloodPressureByTokenPagingRequest request);

    BaseResponse updateBloodPressureById(BaseRequest request);

    BaseResponse updateBloodPressureByToken(BaseRequest request);

    BaseResponse deleteBloodPressureById(BaseRequest request);

    BaseResponse deleteBloodPressureByToken(BaseRequest request);

}
