/**
 * Author : sharuka_j
 * Date : 1/25/2023
 * Time : 7:22 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.handler.annotation.SendTo;

import java.sql.Date;
@Getter
@Setter
public class CommissionTxnBean {
    private String merchantcustid;
    private String custaccountno;
    private String mid;
    private String meraccountno;
    private String tid;
    private String transactionamount;
    private Double merchantcommssion;
    private String merchantdueamount;
    private String currencytype;
    private String crdr;
    private Date transactiondate;
    private String transactiontype;
    private String batchno;
    private String transactionid;
    private String toaccountno;
    private String status;
    private String calmethod;
    private String cardassociation;
    private int bintype;
    private String productid;
    private String segment;
    private String bin;
    private String cardProduct;

    private String calculatedMdrPercentage="0";
    private String calculatedMdrFlatAmount="0";
}
