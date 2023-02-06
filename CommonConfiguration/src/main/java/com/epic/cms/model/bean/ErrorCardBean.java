package com.epic.cms.model.bean;

import com.epic.cms.util.CardAccount;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ErrorCardBean {
    private int eodID;
    private Date eodDate;
    private String customerID;
    private String accountNo;
    private StringBuffer cardNo;
    private String remark;
    private int processId;
    private String processName;
    private int isProcessFails;
    private CardAccount cardAccount;

    public ErrorCardBean() {}

    public ErrorCardBean(int eodID, Date eodDate, StringBuffer cardNo, String remark, int processId, String processName, int isProcessFails, CardAccount accType) {
        this.eodID = eodID;
        this.eodDate = eodDate;
        this.cardAccount=accType;

        this.cardNo = cardNo;
        /*if (accType.equals(CardAccount.CARD)) {
            this.cardNo = cardNo;
        } else if (accType.equals(CardAccount.ACCOUNT)) {
            this.accountNo = cardNo;
        }*/
        this.remark = remark;
        this.processId = processId;
        this.processName = processName;
        this.isProcessFails = isProcessFails;
    }



}
