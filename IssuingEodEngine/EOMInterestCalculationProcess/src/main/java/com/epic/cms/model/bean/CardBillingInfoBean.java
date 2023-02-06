package com.epic.cms.model.bean;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class CardBillingInfoBean {

    private int startEodId;
    private int endEodId;
    private Date statementStartDate;
    private Date statementEndDate;
    private double thisBillingClosingBalance;
    private double thisBillingOpeningBalance;
    private Date dueDate;
    private double minPayDue;


}
