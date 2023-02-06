package com.epic.cms.model.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
@Setter
@Getter
public class CardBean {
    private String billingID;
    private StringBuffer cardnumber;
    private String accountno;
    private String applicationId;
    private String customerid;
    private StringBuffer mainCardNo;
    private String cardtype;
    private String cardProduct;
    private String expiryDate;
    private String nameOnCard;
    private String cardStatus;
    private String accStatus;
    private double creditLimit;
    private double otbCash;
    private double otbCredit;
    private String priorityLevel;
    private Date nextAnniversaryDate;
    private double cashLimit;
    private String availableBalance;
    private String cashAvailableBalance;
    private String tempOtbAmount;
    private String embossStatus;
    private String encodeStatus;
    private String pinStatus;
    private String loyaltyPoint;
    private String offset;
    private String lastUpdatedTime;
    private String lastUpdatedUser;
    private String createdTime;
    private String redeemPoint;
    private String serviceCode;
    private String productionMode;
    private String newExpireDate;
    private String cardKeyId;
    private Date activateDate;
    private Date createdDate;
    private Date nextBillingDate;
    private String cardCategory;
    private double tempcreditamount;
    private double tempcashamount;
    private List<StringBuffer> oldCardNumbers;
    private boolean hasReplacedCards;
    private double interestrate;
    private Date dueDate;

    public boolean getHasReplacedCards() {
        return hasReplacedCards;
    }

}
