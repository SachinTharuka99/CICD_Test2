/**
 * Author : rasintha_j
 * Date : 3/20/2023
 * Time : 5:46 PM
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
