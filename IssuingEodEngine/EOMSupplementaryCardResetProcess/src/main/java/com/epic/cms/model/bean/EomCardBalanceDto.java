/**
 * Author : sharuka_j
 * Date : 12/6/2022
 * Time : 9:08 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EomCardBalanceDto {
    private boolean hasRecordInEOMBalance;
    private double fee;
    private double cashAdvanced;
    private double sales;
}
