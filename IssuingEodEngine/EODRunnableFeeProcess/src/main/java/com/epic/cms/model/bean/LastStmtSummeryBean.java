/**
 * Author :
 * Date : 10/31/2022
 * Time : 2:24 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
public class LastStmtSummeryBean {
    private StringBuffer cardno;
    private Double opaningBalance;
    private Double closingBalance;
    private Double minAmount;
    private Date dueDate;
    private Date statementStartDate;
    private Date statementEndDate;
    private String accNo;
    private int NDIA;// number of days in arias
    private Long closingLoyaltyPoint;
}
