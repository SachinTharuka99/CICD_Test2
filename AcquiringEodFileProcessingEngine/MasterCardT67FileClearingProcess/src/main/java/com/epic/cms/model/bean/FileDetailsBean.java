/**
 * Author : rasintha_j
 * Date : 7/11/2023
 * Time : 8:43 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.model.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileDetailsBean {
    private String fileName;
    private String fileId;
    private String transactionType;
}
