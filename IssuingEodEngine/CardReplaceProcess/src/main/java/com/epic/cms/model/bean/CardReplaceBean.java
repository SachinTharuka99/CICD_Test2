package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardReplaceBean {
    private StringBuffer newCardNo;
    private StringBuffer oldCardNo;
    private String status;
}
