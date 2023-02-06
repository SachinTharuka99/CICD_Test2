package com.epic.cms.model.bean;

import com.epic.cms.util.MerchantCustomer;
import com.epic.cms.util.MerchantCustomerEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ErrorMerchantBean {
    private int eodID;
    private Date eodDate;
    private String mechantID;
    private String remark;
    private int processId;
    private String processName;
    private int isProcessFails;
    private MerchantCustomer merchantCustomer;

    public ErrorMerchantBean(int eodID, Date eodDate, String mechantID, String remark, int processId, String processName, int isProcessFails, MerchantCustomer merchantType) {
        this.eodID = eodID;
        this.eodDate = eodDate;
        this.mechantID = mechantID;
        this.remark = remark;
        this.processId = processId;
        this.processName = processName;
        this.isProcessFails = isProcessFails;
        this.merchantCustomer=merchantType;
    }
}
