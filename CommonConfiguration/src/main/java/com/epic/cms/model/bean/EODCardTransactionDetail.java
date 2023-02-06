package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EODCardTransactionDetail {
    private int EODdate;
    private StringBuffer cardNumber;
    private double totalPayments;
    private double totalSales;
    private double totalFees;
    private double totalInterests;
    private double totalCashAdvances;
}
