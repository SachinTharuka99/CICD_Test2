/**
 * Author : yasiru_l
 * Date : 11/15/2022
 * Time : 5:37 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardTransactionBean {
    private int StartEodID;
    private int EndEodID;
    private String cardNo;
    private double salesAndEasyPayments;
    private double paymentsAndRevarsal;
    private double fee;
    private double cashAdvance;
    private double interest;
    private double totalDrAdj;
    private double totalCrAdj;
    private double creditsWithoutPayments;
}
