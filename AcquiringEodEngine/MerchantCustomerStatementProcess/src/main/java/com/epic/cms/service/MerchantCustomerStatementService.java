/**
 * Author : lahiru_p
 * Date : 6/26/2023
 * Time : 3:40 PM
 * Project Name : ECMS_EOD_PRODUCT
 */

package com.epic.cms.service;

import com.epic.cms.Exception.FailedCardException;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantCustomerBean;
import com.epic.cms.repository.MerchantCustomerStatementRepo;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.MerchantCustomer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;

@Service
public class MerchantCustomerStatementService {

    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");

    @Autowired
    LogManager logManager;

    @Autowired
    MerchantCustomerStatementRepo merchantCustomerStatementRepo;

    public void insertMerchantCustomerStatement(MerchantCustomerBean bean, String merchantCusNo) throws Exception {
        try {
            bean = merchantCustomerStatementRepo.getMerchantCustomerPayments(bean);

            if (!bean.isFirstStatement()) {
                merchantCustomerStatementRepo.getLastmerchantCustomerStatementDetails(bean);
            } else {
                bean.setStartEodId(0);
            }

            String StatementID = new SimpleDateFormat("yyMMHHmmssSSS").format(new java.util.Date()) + bean.getMerchantCusNo();
            bean.setStatementID(StatementID);
            bean.setEndEodId(Configurations.EOD_ID);

            boolean isInsertedMerchantCustomerStatement = merchantCustomerStatementRepo.insertInToMerchantCustomerStatementTable(bean);

            if (!isInsertedMerchantCustomerStatement) {
                throw new FailedCardException("merchant Customer No " + bean.getMerchantCusNo() + "fails to insert data into MERCHANTCUSTOMERSTATEMENT table");
            }

            if (!merchantCustomerStatementRepo.updateMerchantCustomerBillingDate(bean)) {
                logInfo.info("Error Occurred in update next billing date for merchant Customer No " + bean.getMerchantCusNo() + ". ");
            }


        } catch (Exception ex) {
            Configurations.merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, merchantCusNo, ex.getMessage(), Configurations.PROCESS_ID_MERCHANT_CUSTOMER_STATEMENT, "", 0, MerchantCustomer.MERCHANTCUSTOMER));
            //failedMerchantCusCount++;
            Configurations.PROCESS_FAILD_COUNT++;
            logError.error("Error Occurs, when running merchant customer statement process for merchant customer no " + merchantCusNo + " ", ex);
        }
    }
}
