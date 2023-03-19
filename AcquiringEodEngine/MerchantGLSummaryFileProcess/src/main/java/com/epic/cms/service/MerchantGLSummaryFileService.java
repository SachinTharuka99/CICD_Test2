/**
 * Author : sharuka_j
 * Date : 2/1/2023
 * Time : 6:22 AM
 * Project Name : ecms_eod_engine
 */

package com.epic.cms.service;

import com.epic.cms.dao.MerchantGLSummaryFileDao;
import com.epic.cms.model.model.GlAccountBean;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

import static com.epic.cms.util.LogManager.errorLogger;
import static com.epic.cms.util.LogManager.infoLogger;

@Service
public class MerchantGLSummaryFileService {

    @Autowired
    MerchantGLSummaryFileDao merchantGLSummaryFileDao;

    @Autowired
    LogManager logManager;

    @Autowired
    @Qualifier("ThreadPool_100")
    ThreadPoolTaskExecutor taskExecutor;

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void commissionGlFile(GlAccountBean glaccountBean) {
        LinkedHashMap accDetails = new LinkedHashMap();
        if (!Configurations.isInterrupted) {
            try {
                accDetails.put("Merchant ID", glaccountBean.getMerchantID());
                accDetails.put("GL type", glaccountBean.getGlType());
                accDetails.put("CRDR", glaccountBean.getCrDr());
                accDetails.put("Amount", glaccountBean.getGlAmount());
                accDetails.put("ID", glaccountBean.getKey());
                //insert to EODGL table
                merchantGLSummaryFileDao.insertIntoEodMerchantGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getMerchantID(), glaccountBean.getGlType(), glaccountBean.getGlAmount(), glaccountBean.getCrDr());
                merchantGLSummaryFileDao.updateCommissions(glaccountBean.getKey(), 1);
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                Configurations.PROCESS_FAILD_COUNT++;
                logManager.logError("Commission gl file exeption ", e, errorLogger);
                accDetails.put("Sync fail to EOD Merchant GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
            } finally {
                logManager.logDetails(accDetails, infoLogger);
                accDetails.clear();
            }
        }
    }

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createFeeGLFile(GlAccountBean glaccountBean) {
        LinkedHashMap accDetails = new LinkedHashMap();
        if (!Configurations.isInterrupted) {
            try {
                accDetails.put("Merchant ID", glaccountBean.getMerchantID());
                accDetails.put("GL type", glaccountBean.getGlType());
                accDetails.put("CRDR", glaccountBean.getCrDr());
                accDetails.put("Amount", glaccountBean.getGlAmount());
                accDetails.put("ID", glaccountBean.getKey());
                //insert to EODGL table
                merchantGLSummaryFileDao.insertIntoEodMerchantGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getMerchantID(), glaccountBean.getGlType(), glaccountBean.getGlAmount(), glaccountBean.getCrDr());
                merchantGLSummaryFileDao.updateMerchantFeeGlStatus(glaccountBean.getKey(), 1);
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                Configurations.PROCESS_FAILD_COUNT++;
                logManager.logError("Create fee gl file exeption ", e, errorLogger);
                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
            } finally {
                logManager.logDetails(accDetails, infoLogger);
                accDetails.clear();
            }
        }
    }

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createEODMerchantTxnTableGLFile(GlAccountBean glaccountBean) {
        LinkedHashMap accDetails = new LinkedHashMap();
        if (!Configurations.isInterrupted) {
            try {
                accDetails.put("Merchant ID", glaccountBean.getMerchantID());
                accDetails.put("GL type", glaccountBean.getGlType());
                accDetails.put("CRDR", glaccountBean.getCrDr());
                accDetails.put("Amount", glaccountBean.getGlAmount());
                accDetails.put("ID", glaccountBean.getKey());
                //Insert to EODGL table
                merchantGLSummaryFileDao.insertIntoEodMerchantGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getMerchantID(), glaccountBean.getGlType(), glaccountBean.getGlAmount(), glaccountBean.getCrDr());
                merchantGLSummaryFileDao.updateEODMerchantTxn(glaccountBean.getKey(), 1);
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                Configurations.PROCESS_FAILD_COUNT++;
                logManager.logError("Exeption ", e, errorLogger);
                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
            } finally {
                logManager.logDetails(accDetails, infoLogger);
                accDetails.clear();
            }
        }
    }

    @Async("taskExecutor2")
    @Transactional(value = "transactionManager", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void createMerchantPaymentTableGLFile(GlAccountBean glaccountBean) {
        LinkedHashMap accDetails = new LinkedHashMap();
        if (!Configurations.isInterrupted) {
            try {
                accDetails.put("Merchant ID", glaccountBean.getMerchantID());
                accDetails.put("GL type", glaccountBean.getGlType());
                accDetails.put("CRDR", glaccountBean.getCrDr());
                accDetails.put("Amount", glaccountBean.getGlAmount());
                accDetails.put("ID", glaccountBean.getKey());
                //Insert to EODGL table
                merchantGLSummaryFileDao.insertIntoEodMerchantGLAccount(Configurations.EOD_ID, Configurations.EOD_DATE, glaccountBean.getMerchantID(), glaccountBean.getGlType(), glaccountBean.getGlAmount(), glaccountBean.getCrDr());
                merchantGLSummaryFileDao.updateEODMerchantPayment(glaccountBean.getKey(), 1);
                Configurations.PROCESS_SUCCESS_COUNT++;
            } catch (Exception e) {
                Configurations.PROCESS_FAILD_COUNT++;
                logManager.logError("Exeption ", e, errorLogger);
                accDetails.put("Sync fail to EOD GL Account Table for Primary ID " + glaccountBean.getKey(), glaccountBean.getKey());
            } finally {
                logManager.logDetails(accDetails, infoLogger);
                accDetails.clear();
            }
        }
    }
}
