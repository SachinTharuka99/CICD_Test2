/**
 * Author : rasintha_j
 * Date : 3/20/2023
 * Time : 6:57 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.common;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class Common {

    public static String searchAuditString(String description, String fieldName, String str) {
        if (description.isEmpty() && str != null && !str.isEmpty()) {
            description = fieldName + " - " + str;
        } else if (str != null && !str.isEmpty()) {
            description = description + ", " + fieldName + " - " + str;
        }
        return description;
    }

    public List<Order> getSort(String[] sort) {
        List<Order> orders = new ArrayList<Order>();

        if (sort[0].contains(",")) {
            for (String sortOrder : sort) {
                String[] _sort = sortOrder.split(",");
                orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
            }
        } else {
            orders.add(new Order(getSortDirection(sort[1]), sort[0]));
        }

        return orders;
    }

    private Direction getSortDirection(String direction) {
        if (direction.equals("asc")) {
            return Direction.ASC;
        } else if (direction.equals("desc")) {
            return Direction.DESC;
        }
        return Direction.ASC;
    }
}
