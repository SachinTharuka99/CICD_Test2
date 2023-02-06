/**
 * Author : sharuka_j
 * Date : 1/27/2023
 * Time : 7:04 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Setter
@Getter
public class TerminalBeanForFee {
    String terminalId;
    Date nextAnniversaryDate; // for terminal annual maintenance fee
    Date nextRentalDate;      // for terminal monthly rental
    int terminalType;         // logical terminal=0 , physical terminal=1
    String terminalStatus;
    Date nextBiMonthlyDate; // for terminal bi monthly rental fee
    Date nextQuarterlyDate; // for terminal quarterly rental fee
    Date nextHalfYearlyDate; //for terminal half yearly rental fee
    Date nextWeeklyDate; //for terminal weekly rental fee
}
