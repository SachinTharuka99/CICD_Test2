package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardAccountCustomerBean {
    private String customerId;
    private String accountNumber;
    private StringBuffer cardNumber;
    private StringBuffer maincardNumber;
    private String cardType;
    private String primaryStatus;
}
