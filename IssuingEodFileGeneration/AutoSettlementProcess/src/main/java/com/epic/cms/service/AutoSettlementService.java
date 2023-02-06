/**
 * Author : lahiru_p
 * Date : 11/28/2022
 * Time : 10:07 AM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.util.Configurations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static com.epic.cms.util.CommonMethods.*;

@Service
public class AutoSettlementService {

    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public StringBuilder createFileHeaderForAutoSettlementFile(String fileName, BigDecimal headerTotalAmount, int totalFileTxnCount, String sequence, String fieldDelimeter) throws Exception {
        StringBuilder sbHeader = new StringBuilder();
        try {
            sbHeader.append(validateLength(fileName, 65)); /**File Name*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(Configurations.CHANNEL_ID_AUTOSETTLEMENT, 20)); /**CHANNEL_ID*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 35)); /**PROD_CODE*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(sequence, 35)); /**BATCH_FILE_REFERENCE*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("N", 1)); /**BATCH_REJ*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(String.valueOf(totalFileTxnCount), 4)); /**NO_OF_DR*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateCurrencyLength(headerTotalAmount.toString(), 30)); /**DR_TOT_VAL_LCY*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("", 4)); /**NO_OF_CR*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("", 30)); /**CR_TOT_VAL_LCY*/
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("-1", 50)); /**CHK_SUM*/
            sbHeader.append(System.lineSeparator());

        }catch (Exception e){
            throw e;
        }
        return sbHeader;
    }
}
