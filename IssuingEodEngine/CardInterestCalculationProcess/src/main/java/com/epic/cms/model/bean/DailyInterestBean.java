/**
 * Created By Lahiru Sandaruwan
 * Date : 10/24/2022
 * Time : 8:57 PM
 * Project Name : ecms_eod_engine
 * Topic : dailyInterestCalculation
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyInterestBean {
    private double amount; // transaction or payment amount
    private int noOfDays;  // no of days from transaction/payment date to interest calculation date
}
