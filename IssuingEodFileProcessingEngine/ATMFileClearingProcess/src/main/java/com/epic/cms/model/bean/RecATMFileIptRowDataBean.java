/**
 * Author :
 * Date : 2/2/2023
 * Time : 11:25 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecATMFileIptRowDataBean {
    private String fileid;
    private String filename;
    private BigDecimal linenumber;
    private String linecontent;
}
