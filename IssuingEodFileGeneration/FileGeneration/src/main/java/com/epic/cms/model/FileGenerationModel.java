/**
 * Author : lahiru_p
 * Date : 11/15/2022
 * Time : 4:15 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;

@Getter
@Setter
public class FileGenerationModel {
    private StringBuilder finalFile;
    private StringBuilder fileHeader;
    private StringBuilder fileContent;
    private ArrayList<Integer> txnIdList;
    BigDecimal headerCreditBig;
    BigDecimal headerDebitBig;
    int headerCreditCount;
    int headerDebitCount;
    int totalFileTxnCount;
    private Boolean status;
    private Boolean deleteStatus;
}
