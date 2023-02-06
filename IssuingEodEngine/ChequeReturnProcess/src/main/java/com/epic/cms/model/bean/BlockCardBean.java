package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlockCardBean {
    private StringBuffer CardNo;
    private String CardStatus;
    private String oldStatus;
    private String newStatus;
    private String blockReason;
}
