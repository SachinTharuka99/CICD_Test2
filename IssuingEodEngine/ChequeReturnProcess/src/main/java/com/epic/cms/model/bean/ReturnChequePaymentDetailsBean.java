/**
 * Author : rasintha_j
 * Date : 1/24/2023
 * Time : 11:48 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;


@Getter
@Setter
public class ReturnChequePaymentDetailsBean {
    private int id;
    private int eodid;
    private Date chqRtnDate;
    private StringBuffer cardnumber;
    private StringBuffer oldcardnumber;
    private StringBuffer maincardno;
    private String accountNo;
    private String customerid;
    private double amount;
    private Date chequedate;
    private String chequenumber;
    private double minamount;
    private double forwardinterest;
    private double interestrate;
    private double closingbalance;
    private double otbcredit;
    private double otbcash;
    private String chequestatus;
    private String delinquentclass;
    private String CHEQUE_RET_CODE;
    private String cardstatus;
    private Date duedate;
    private Date statementstartdate;
    private Date statementenddate;
    private int statementstarteodid;
    private int statementendeodid;
    private String returnreason;
    private String traceid;
    private String seqNo;
    private String cqrtseqNo;
    private double totalNetBalanceForCard;
    private double mainFinChargeKnockoff;
    private double mainCashAdvanceKnockoff;
    private double mainTransactionKnockoff;
    private double supFinChargeKnockoff;
    private double supCashAdvanceKnockoff;
    private double supTransactionKnockoff;
    private String isPrimary;
    private int ndia;
}
