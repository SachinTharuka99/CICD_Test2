package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentFileDataBean {
    private String fileid;
    private String filename;
    private BigDecimal linenumber;
    private String linecontent;
}
