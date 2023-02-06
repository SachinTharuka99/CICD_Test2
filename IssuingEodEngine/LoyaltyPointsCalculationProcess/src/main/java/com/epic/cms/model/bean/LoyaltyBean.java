package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LoyaltyBean {
    private double openingLoyaltyPoints;
    private double earnLoyaltyPoints;
    private double availableLoyaltyPoints;
    private double adjustLoyaltyPoints;
    private double redeemLoyaltyPoints;
    private double closingLoyaltyPoints;
    private double purchase;
    private StringBuffer cardNo;
    private String accNo;
    private String statementId;
    private int stmtStartEodID;
    private int stmtEndEodID;
    private Date stmtStartDate;
    private Date stmtEndDate;
}
