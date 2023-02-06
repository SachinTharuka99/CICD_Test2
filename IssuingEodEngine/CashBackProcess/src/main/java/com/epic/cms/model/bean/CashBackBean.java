package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Setter
@Getter
public class CashBackBean {
    private String accountNumber;
    private String accountStatus;
    private StringBuffer mainCardNumber;
    private String mainCardStatus;
    private Date statementDate;
    private String cashbackProfileCode;
    private Date cashbackStartDate;
    private Date nextCashbackStartDate; //cashbackStartDate+year
    private Date nextCBRedeemDate;
    private BigDecimal availableCashbackAmount;
    private Double redeemRatio;
    private Double minSpendPerMonth;
    private Double maxCashbackPerYear;
    private Double cashbackRate;
    private String creditOption;
    private int cashbackExpiryPeriod;
    private Date lastCashbackDate;
    private Double minAccumulatedToClaim;
}
