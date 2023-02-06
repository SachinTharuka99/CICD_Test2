package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class StatementBean {
    private String billingID;
    private String statementID;
    private int StartEodID;
    private int EndEodID;
    private String accountNo;
    private StringBuffer cardNo;
    private double closingBalance;
    private double LastBillclosingBalance;
    private double startingBalance;

    private StringBuffer mainCardNo;
    private String cardCategory;

    private Date statementStartDate;
    private Date statementEndDate;
    private Date statementDueDate;
    private Date oldNextBillingDate;
    private Date newNextBillingDate;
    private Date cardActivationDate;
    private Date cardCreatedDate;

    private double creditLimit;
    private double cashLimit;
    private double availablCereditLimit;
    private double availableCashLimit;
    private double totalMinPayment;
    private double totalMinPaymentDue;
    private double openBalance;
    private double totalPurchases;
    private double financeCharges;
    private double otherCharges;
    private double paymentAndCredit;
    private double interest;
    private double fee;
    private double cashAdvance;
    private double totalDrAdj;
    private double totalCrAdj;
    private double totalCreditsWithoutPayments;


    private int openLoyaltyPoint;
    private int earnedLoyaltyPoint;
    private int adjustLoyaltyPoint;
    private int redeemLoyaltyPoint;
    private int closingLoyaltyPoint;
    private int stampDuity;
    private int isAttachedMainStatement;

    private String title;
    private String nameOnCard;
    private String address1;
    private String address2;
    private String address3;
    private String city;

    private boolean hasBillingCycleChangeRequest;
    private boolean isFirstStatement;
    private boolean isLastOperationSucessful;
    private boolean isInsertedBillingLastStatementSummry;
    private boolean isInsertedBillingStatement;
    private boolean isUpdatedBillingLastStatementSummry;

    public boolean getHasBillingCycleChangeRequest() {
        return hasBillingCycleChangeRequest;
    }
}
