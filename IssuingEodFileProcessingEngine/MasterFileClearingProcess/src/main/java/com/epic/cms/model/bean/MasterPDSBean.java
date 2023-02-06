/**
 * Author :
 * Date : 2/3/2023
 * Time : 11:36 PM
 * Project Name : ecms_eod_file_processing_engine
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasterPDSBean {
    private String sessionId = null;
    private String fileId = null;
    private String txnId = null;
    private String mti = null;
    private String fieldId = null;
    private String pds = null;
    private String length = null;
    private String data = null;
    private String status = null;
}
