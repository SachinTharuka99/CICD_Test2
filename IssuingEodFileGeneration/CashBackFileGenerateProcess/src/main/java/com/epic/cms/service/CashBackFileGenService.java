/**
 * Author : lahiru_p
 * Date : 11/29/2022
 * Time : 11:38 PM
 * Project Name : ecms_eod_file_generation_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.GlAccountBean;
import com.epic.cms.util.Configurations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.epic.cms.util.CommonMethods.*;
import static com.epic.cms.util.LogManager.errorLogger;

@Service
public class CashBackFileGenService {
    String fieldDelimeter = ",";
    String fieldDelimeterPipe = "|";

    public StringBuilder addFirstFileContent(GlAccountBean glAccountBean, BigDecimal cashBackRedeem, String seqNo, String today3, SimpleDateFormat sdf5, Date nextWorkingDay) {
        StringBuilder content = new StringBuilder();
        try {
            content.append(validateLength(seqNo, 35)); //SEQ_NO
            content.append(fieldDelimeter);
            content.append(validateLength(Configurations.OUTPUT_FILE_PROD_CODE, 35)); //PROD_CODE
            content.append(fieldDelimeter);
            content.append(validateLength(today3, 11)); //TRANSACTION_DATE
            content.append(fieldDelimeter);
            content.append(validateCurrencyLength(cashBackRedeem.toString(), 19)); //CR_AMOUNT -- totalPaymentAmountString
            content.append(fieldDelimeter);
            content.append(validateLength("LKR", 3)); //PAYMENT_CURRENCY --validateLength(currencyList.get(bean.getCurrencyCode()).toString(), 3)
            content.append(fieldDelimeter);
            content.append(validateLength(glAccountBean.getAccNo(), 24)); //CR_ACCOUNT --bean.getAccountNo()
            content.append(fieldDelimeter);
            content.append(validateLength("", 17)); //BEN_ID
            content.append(fieldDelimeter);
            content.append(validateLength("", 34)); //BEN_ACCOUNT
            content.append(fieldDelimeter);
            content.append(validateLength("", 71)); //BENEFICIARY_NAME
            content.append(fieldDelimeter);
            content.append(validateLength(today3, 11)); //CR_VALUE_DATE
            content.append(fieldDelimeter);
            content.append(validateLength("CB: " + sdf5.format(nextWorkingDay).toUpperCase() + " " + glAccountBean.getAccNo() + " **" + new StringBuffer(glAccountBean.getCardNo()).substring(12), 45)); //CR_NARRATION -- mid
            content.append(fieldDelimeter);
            content.append(validateLength("", 35)); //Bank Code+Branch Code
            content.append(fieldDelimeter);
            content.append(validateLength("", 10)); //Tran Code
            content.append(fieldDelimeter);
            content.append(validateLength("", 35)); //Host Deal Reference
            content.append(fieldDelimeter);
            content.append(validateLength("", 6)); //Purpose Code-1
            content.append(fieldDelimeter);
            content.append(validateLength("", 10)); //Purpose Code-2
            content.append(fieldDelimeter);
            content.append(validateLength("", 35)); //additional_reference_no_1
            content.append(fieldDelimeter);
            content.append(validateLength("", 3)); //Chargers waive (Y/N)
            content.append(fieldDelimeter);
            content.append(validateLength("", 35)); //Charge Bearer
            content.append(fieldDelimeter);
            content.append(validateLength("", 75)); //CHARGE TYPE
            content.append(fieldDelimeter);
            content.append(validateLength("", 19)); //Charge Amount
            content.append(fieldDelimeter);
            content.append(validateLength("", 35)); //EXTERNAL_REF
            content.append(fieldDelimeter);

            content.append(System.lineSeparator());
        } catch (Exception e) {
            errorLogger.error("Exception in adding first file content" , e);
        }
        return content;
    }

    public StringBuilder addSecondFileContent(GlAccountBean glAccountBean, String debitAccount, BigDecimal cashBackRedeem, SimpleDateFormat sdf6, SimpleDateFormat sdf7, int recordCount) {
        StringBuilder content2 = new StringBuilder();
        try {
            content2.append(validateLength(debitAccount, 34)); //DEBIT.ACCT.NO
            content2.append(fieldDelimeter);
            content2.append(validateLength("LKR", 3)); //DEBIT.CCY
            content2.append(fieldDelimeter);
            content2.append(validateCurrencyLength(cashBackRedeem.toString(), 19)); //DEBIT.AMOUNT
            content2.append(fieldDelimeter);
            content2.append(validateLength("Cash back " + sdf6.format(new Date()), 15));//DEBIT_THEIR_REF
            content2.append(fieldDelimeter);
            content2.append(validateLength("", 17)); //CUSTOMER.RATE
            content2.append(fieldDelimeter);
            content2.append(validateLength(glAccountBean.getAccNo(), 24)); //CREDIT.ACCT.NO --bean.getAccountNo()
            content2.append(fieldDelimeter);
            content2.append(validateLength("", 3)); //CREDIT.CCY --To be pass only for cross currency transaction
            content2.append(fieldDelimeter);
            content2.append(validateLength("", 19)); //CREDIT.AMOUNT --Should not pass any value
            content2.append(fieldDelimeter);
            content2.append(validateLength("Cash back " + sdf6.format(new Date()), 15));//CREDIT.THEIR.REF
            content2.append(fieldDelimeter);
            content2.append(validateLength("CB" + sdf7.format(new Date()).toUpperCase(), 5)); //BATCH REF
            content2.append(fieldDelimeterPipe);

            String tranRef = String.format("%05d", recordCount);
            content2.append(validateLength(tranRef, 15)); //TRAN REF
            content2.append(fieldDelimeter);

            content2.append(validateLength("DFCC", 4)); //ORDERING.CUST
            content2.append(fieldDelimeter);
            content2.append(validateLength("3200", 6)); //PROFIT.CENTRE

            content2.append(System.lineSeparator());
        } catch (Exception e) {
            errorLogger.error("Exception in adding second file content" , e);
        }
        return content2;
    }

    public StringBuilder addFirstFileHeader(String debitAccount, String fileNameF1, SimpleDateFormat sdf5, String today3, BigDecimal headerCreditBig, int headerCreditCount) {
        StringBuilder sbHeader = new StringBuilder();
        try {
            sbHeader.append(validateLength(fileNameF1, 125)); //BATCH_FILE_REFERENCE
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(Configurations.BULK_TYPE_MULTI, 10)); //BULK_TYPE
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(today3, 11)); //PROCESSING_DATE
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("LKR", 3)); //Payment Currency
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(debitAccount, 34)); //DR_ACCOUNT -- Configurations.MERCHANT_PAYABLE_GL_SLIPS
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(today3, 11)); //DR_VALUE_DATE
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("Cash back " + sdf5.format(new Date()), 35)); //DR_NARRATION -- mId
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateCurrencyLength(headerCreditBig.toString(), 19)); //Total Transactions
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(Integer.toString(headerCreditCount), 19)); //NO of Transactions
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(fileNameF1, 35)); //File Name
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength(Configurations.CHANNEL_ID_CASHBACK, 30)); //CHANNEL_ID
            sbHeader.append(fieldDelimeter);
            sbHeader.append(validateLength("1", 10)); //CHK_SUM
            sbHeader.append(System.lineSeparator());
        } catch (Exception e) {
            errorLogger.error("Exception in adding first file header" , e);
        }
        return sbHeader;
    }

    public StringBuilder addSecondFileHeader(String today3, SimpleDateFormat sdf6) {
        StringBuilder sbHeader2 = new StringBuilder();
        try {
            String fileNamePrefix = Configurations.CASHBACK_FILE_PREFIX_F2;
            sbHeader2.append(validateLength(fileNamePrefix.split("\\.")[0], 15)); //BATCH FILE REF
            sbHeader2.append(fieldDelimeter);
            sbHeader2.append(validateLength("Cash back " + sdf6.format(new Date()), 15));//DESCRIPTION
            sbHeader2.append(fieldDelimeter);
            sbHeader2.append(validateLength(Configurations.BULK_TYPE_SINGLE, 9)); //BULK TYPE
            sbHeader2.append(fieldDelimeter);
            sbHeader2.append(validateLength(Configurations.TRAN_TYPE_CREDIT, 6)); //TRAN TYPE
            sbHeader2.append(fieldDelimeter);
            sbHeader2.append(validateLength(Configurations.UPLOADED_METHOD_SINGLE, 6)); //UPLOAD METORD
            sbHeader2.append(fieldDelimeter);
            sbHeader2.append(validateLength(today3, 11)); //VALUE DATE
            sbHeader2.append(fieldDelimeter);
            sbHeader2.append(validateLength(fileNamePrefix.split("\\.")[0], 15)); //NARRATION
            sbHeader2.append(fieldDelimeter);

            sbHeader2.append(System.lineSeparator());
        }catch (Exception e){
            errorLogger.error("Exception in adding second file header" , e);
        }
        return sbHeader2;
    }
}
