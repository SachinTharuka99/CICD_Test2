/**
 * Author :
 * Date : 2/3/2023
 * Time : 4:00 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VisaTC56CurrencyEntryBean {
    private String actionCode;
    private String counterCurrencyCode;
    private String baseCurrencyCode;
    private String effectiveDate;
    private int buyScaleFactor;
    private int SellScaleFactor;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
}
