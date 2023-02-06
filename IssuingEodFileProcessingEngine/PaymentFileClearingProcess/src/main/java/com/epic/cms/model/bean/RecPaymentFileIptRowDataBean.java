/**
 * Author :
 * Date : 2/3/2023
 * Time : 1:42 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecPaymentFileIptRowDataBean {
    private String fileid;
    private String filename;
    private BigDecimal linenumber;
    private String linecontent;
}
