/**
 * Author : rasintha_j
 * Date : 3/31/2023
 * Time : 12:00 PM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DataTableBean {
    private long count;
    private long pagecount;
    private List<Object> list;
}
