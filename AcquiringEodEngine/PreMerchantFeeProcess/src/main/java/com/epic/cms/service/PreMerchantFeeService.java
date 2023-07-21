/**
 * Author : sharuka_j
 * Date : 1/26/2023
 * Time : 12:52 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.PreMerchantFeeDao;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantBeanForFee;
import com.epic.cms.model.bean.TerminalBeanForFee;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import com.epic.cms.util.MerchantCustomer;
import com.epic.cms.util.StatusVarList;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static com.epic.cms.util.Configurations.merchantErrorList;

@Service
public class PreMerchantFeeService {
    private static final Logger logInfo = LoggerFactory.getLogger("logInfo");
    private static final Logger logError = LoggerFactory.getLogger("logError");
    public LinkedHashMap details = new LinkedHashMap();
    @Autowired
    StatusVarList status;
    @Autowired
    LogManager logManager;
    @Autowired
    PreMerchantFeeDao preMerchantFeeDao;

    @Async("ThreadPool_100")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void preMerchantFee(MerchantBeanForFee merchantBean, HashMap<String, List<String>> feeCodeMap) {
        try {
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            String feeProfileCode = merchantBean.getFeeProfile();
            if (feeProfileCode != null) {
                List<String> eligibleFeeCodeList = feeCodeMap.get(feeProfileCode); //get the eligible fee code list for the merchant
                if (eligibleFeeCodeList != null) {
                    Configurations.PROCESS_TOTAL_NOOF_TRABSACTIONS += eligibleFeeCodeList.size();
                    for (String feeCode : eligibleFeeCodeList) {
                        if (feeCode.equalsIgnoreCase(Configurations.MERCHANT_ANNUAL_FEE) || feeCode.equalsIgnoreCase(Configurations.MERCHANT_BI_MONTHLY_FEE)
                                || feeCode.equalsIgnoreCase(Configurations.MERCHANT_QUARTERLY_FEE) || feeCode.equalsIgnoreCase(Configurations.MERCHANT_HALF_YEARLY_FEE)) { //merchant recurring fees
                            int isApplied = applyMerchantRecurringFee(merchantBean, dateformat, feeCode);
                            if (isApplied == 1) {
//                                success_merchant_recurring_fee_count++;
                                Configurations.PROCESS_SUCCESS_COUNT++;
                            }

                        } else if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_MONTHLY_RENTAL_FEE) || feeCode.equalsIgnoreCase(Configurations.TERMINAL_MAINTAINACE_FEE)
                                || feeCode.equalsIgnoreCase(Configurations.TERMINAL_BI_MONTHLY_RENTAL_FEE) || feeCode.equalsIgnoreCase(Configurations.TERMINAL_QUARTERLY_RENTAL_FEE)
                                || feeCode.equalsIgnoreCase(Configurations.TERMINAL_HALF_YEARLY_RENTAL_FEE) || feeCode.equalsIgnoreCase(Configurations.TERMINAL_WEEKLY_RENTAL_FEE)) { //terminal recurring fees
                            List<TerminalBeanForFee> terminalList = merchantBean.getTerminalList(); //get terminal list for merchant
                            if (terminalList != null) {
                                for (TerminalBeanForFee terminalBean : terminalList) {
                                    //terminal need to be a physical terminal and not in delete status
                                    if (terminalBean.getTerminalType() == 1 && !terminalBean.getTerminalStatus().equals(status.getTERMINAL_DELETE_STATUS())) {
                                        //add monthly rental fee
                                        int isApplied = applyTerminalRecurringFee(merchantBean.getMerchantId(), terminalBean, dateformat, feeCode);
                                        if (isApplied == 1) {
//                                            success_terminal_recurring_count++;
                                            Configurations.PROCESS_FAILD_COUNT++;
                                        }
                                    }
                                }
                            }

                        }
                    }
                } else { //fee code list not found
                    logError.error("No fee code list found for " + feeProfileCode);
                }

            } else { //fee profile code is not defined for merchant
                Configurations.PROCESS_FAILD_COUNT++;
                logError.error("No fee profile code define for merchant" + merchantBean.getMerchantId());
//                merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, merchantBean.getMerchantId(), "No fee profile code defined", configProcess, processHeader, 0, MerchantCustomer.MERCHANTLOCATION));
            }
        } catch (Exception ex) {
            Configurations.PROCESS_FAILD_COUNT++;
            logError.error("Error occurred", ex);
            merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, merchantBean.getMerchantId(), ex.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, MerchantCustomer.MERCHANTLOCATION));
        }
    }


    private int applyMerchantRecurringFee(MerchantBeanForFee merchantBean, SimpleDateFormat dateformat, String feeCode) throws Exception {
        Date nextRecurringDate = null;
        int isApplied = 0;
        if (feeCode.equalsIgnoreCase(Configurations.MERCHANT_ANNUAL_FEE)) {
            nextRecurringDate = merchantBean.getNextAnniversaryDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.MERCHANT_BI_MONTHLY_FEE)) {
            nextRecurringDate = merchantBean.getNextBiMonthlyDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.MERCHANT_QUARTERLY_FEE)) {
            nextRecurringDate = merchantBean.getNextQuarterlyDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.MERCHANT_HALF_YEARLY_FEE)) {
            nextRecurringDate = merchantBean.getNextHalfYearlyDate();
        }
        //check nextRecurringDate is equal or before the eod date
        if (nextRecurringDate != null && (nextRecurringDate.toString().equals(dateformat.format(Configurations.EOD_DATE)) || nextRecurringDate.before(Configurations.EOD_DATE))) {
            details.put("Applying merchant " + feeCode + " fee for the merchant ", merchantBean.getMerchantId());
            isApplied = preMerchantFeeDao.addMerchantFeeCount(merchantBean.getMerchantId(), feeCode);
        }
        logInfo.info(logManager.logDetails(details));
        details.clear();
        return isApplied;
    }

    private int applyTerminalRecurringFee(String merchantId, TerminalBeanForFee terminalBean, SimpleDateFormat dateformat, String feeCode) throws Exception {
        Date nextRecurringDate = null;
        int isApplied = 0;
        if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_MAINTAINACE_FEE)) {
            nextRecurringDate = terminalBean.getNextAnniversaryDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_MONTHLY_RENTAL_FEE)) {
            nextRecurringDate = terminalBean.getNextRentalDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_BI_MONTHLY_RENTAL_FEE)) {
            nextRecurringDate = terminalBean.getNextBiMonthlyDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_QUARTERLY_RENTAL_FEE)) {
            nextRecurringDate = terminalBean.getNextQuarterlyDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_HALF_YEARLY_RENTAL_FEE)) {
            nextRecurringDate = terminalBean.getNextHalfYearlyDate();
        } else if (feeCode.equalsIgnoreCase(Configurations.TERMINAL_WEEKLY_RENTAL_FEE)) {
            nextRecurringDate = terminalBean.getNextWeeklyDate();
        }
        //check nextRecurringDate is equal or before the eod date
        if (nextRecurringDate != null && (nextRecurringDate.toString().equals(dateformat.format(Configurations.EOD_DATE)) || nextRecurringDate.before(Configurations.EOD_DATE))) {
            details.put("Applying terminal " + feeCode + " fee for the merchant ", merchantId);
            isApplied = preMerchantFeeDao.addMerchantFeeCount(merchantId, feeCode);
        }

        logInfo.info(logManager.logDetails(details));
        details.clear();
        return isApplied;
    }
}
