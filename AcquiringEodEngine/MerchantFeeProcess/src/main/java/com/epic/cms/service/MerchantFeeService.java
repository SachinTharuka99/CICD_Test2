/**
 * Author : rasintha_j
 * Date : 1/24/2023
 * Time : 12:54 PM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.ErrorCardBean;
import com.epic.cms.model.bean.ErrorMerchantBean;
import com.epic.cms.model.bean.MerchantFeeBean;
import com.epic.cms.repository.CommonRepo;
import com.epic.cms.repository.MerchantFeeRepo;
import com.epic.cms.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;

import static com.epic.cms.util.Configurations.merchantErrorList;
import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class MerchantFeeService {
    @Autowired
    LogManager logManager;

    @Autowired
    MerchantFeeRepo merchantFeeRepo;

    @Autowired
    StatusVarList status;

    @Autowired
    CommonRepo commonRepo;

    @Async("ThreadPool_100")
    @Transactional(value="transactionManager",propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public void MerchantFee(MerchantFeeBean merchantFeeBean) {
        if (!Configurations.isInterrupted) {
            LinkedHashMap details = new LinkedHashMap();
            double feeAmount = 0.0;
            Date effectDate = DateUtil.getSqldate(Configurations.EOD_DATE);
            try {
                //ogger.info("Merchant Id:" + merchantFeeBean.getMID());

                feeAmount = calculateFeeAmount(merchantFeeBean.getMID(), merchantFeeBean.getFeeCount(), merchantFeeBean.getFlatFee(), merchantFeeBean.getMaxAmount(), merchantFeeBean.getMinAmount(), merchantFeeBean.getCombination(), merchantFeeBean.getPercentageAmount(), merchantFeeBean.getCashAmount());
                feeAmount = Double.parseDouble(CommonMethods.ValuesRoundup(feeAmount));
                merchantFeeRepo.insertToEODMerchantFee(merchantFeeBean, feeAmount, effectDate);
                //update status EDON and feecount as 0
                merchantFeeRepo.updateMerchantFeecount(merchantFeeBean);

                details.put("Merchant Id: ", merchantFeeBean.getMID());
                details.put("fee type", merchantFeeBean.getFeeCode());
                details.put("fee count", merchantFeeBean.getFeeCount());
                details.put("flat fee", merchantFeeBean.getFlatFee());
                details.put("fee MIN/MAX/CMB", merchantFeeBean.getCombination());
                details.put("final amount", feeAmount);
                infoLogger.info(logManager.processDetailsStyles(details));
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                errorLogger.error("Commission calculation process failed for merchantId:" + merchantFeeBean.getMID(), e);
                merchantErrorList.add(new ErrorMerchantBean());
                Configurations.merchantErrorList.add(new ErrorMerchantBean(Configurations.ERROR_EOD_ID, Configurations.EOD_DATE, merchantFeeBean.getMID(), e.getMessage(), Configurations.RUNNING_PROCESS_ID, Configurations.RUNNING_PROCESS_DESCRIPTION, 0, MerchantCustomer.MERCHANTLOCATION));
                Configurations.PROCESS_FAILD_COUNT++;
            }
        }
    }

    private double calculateFeeAmount(String mid, int feeCount, double flatFee, double max, double min, String combination, double percentage, double cashAmount) {
        double perc_amount = percentage * cashAmount / 100;
        double amount = CommonMethods.getAmountfromCombination(perc_amount, flatFee * feeCount, combination);
        if (amount >= max) {
            amount = max;
        } else if (amount <= min) {
            amount = min;
        }
        return amount;
    }
}
