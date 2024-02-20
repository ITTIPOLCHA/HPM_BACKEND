package com.gj.hpm.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.gj.hpm.dto.request.BasePaginationRequest;

public class MongoUtil {

    public static Sort getSortFromRequest(BasePaginationRequest request) {
        List<Sort.Order> orders = new ArrayList<>();

        if (request != null && request.getSortBy() != null && request.getSortBy().size() > 0) {
            request.getSortBy().forEach(sort -> {
                orders.add(new Sort.Order(Sort.Direction.fromString(sort.getDirection()), sort.getProperty()));
            });

        }

        return Sort.by(orders);
    }
}
