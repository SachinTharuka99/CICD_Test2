/**
 * Author : sharuka_j
 * Date : 11/22/2022
 * Time : 3:31 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class RiskCalculationBean {
    private StringBuffer cardNo;
    private Date dueDate;
    private double dueAmount;
}