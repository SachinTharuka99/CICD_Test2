package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CollectionAndRecoveryBean {
    private StringBuffer cardNo;
    private double dueAmount;
    private String dueDate;
    private String lastTriger;
}
