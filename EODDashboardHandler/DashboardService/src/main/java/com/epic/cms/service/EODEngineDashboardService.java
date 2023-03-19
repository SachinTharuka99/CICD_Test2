/**
 * Author : rasintha_j
 * Date : 3/18/2023
 * Time : 6:40 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.model.bean.*;
import com.epic.cms.model.entity.*;
import com.epic.cms.repository.*;
import com.epic.cms.util.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class EODEngineDashboardService {

    @Autowired
    EodIdInfoRepo eodIdInfoRepo;

    @Autowired
    ProcessSummeryRepo processSummeryRepo;

    @Autowired
    RecAtmFileInvalidRepo atmFileInvalidRepo;

    @Autowired
    RecPaymentFileInvalidRepo paymentFileInvalidRepo;


    @Autowired
    EodErrorMerchantListRepo eodErrorMerchantListRepo;

    @Autowired
    EodErrorCardListRepo eodErrorCardListRepo;

    @Autowired
    LogManager logManager;

    public EodBean getEodInfoList(Long eodId) {

        EodBean eodBean = new EodBean();
        EOD eod1 = new EOD();
        try {
            Optional<EOD> eodInfo = eodIdInfoRepo.findById(eodId);

            eodInfo.ifPresent(eod -> {
                eodBean.setEodId(eod.getEODID());
                eodBean.setStartTime(eod.getSTARTTIME());
                eodBean.setEndTime(eod.getENDTIME());
                eodBean.setStatus(eod.getSTATUS().getSTATUSCODE());
                eodBean.setSubEodStatus(eod.getSUBEODSTATUS());
                eodBean.setNoOfSuccessProcess(eod.getNOOFSUCCESSPROCESS());
                eodBean.setNoOfErrorProcess(eod.getNOOFERRORPAROCESS());
            });
        } catch (Exception e) {
            throw e;
        }
        return eodBean;
    }

    public NextRunningEodBean getNextRunningEodId() throws Exception {
        NextRunningEodBean nextRunningEodBean = new NextRunningEodBean();
        try {
            nextRunningEodBean.setEodId(eodIdInfoRepo.findByNextRunnindEodId());

        } catch (Exception e) {
            throw e;
        }
        return nextRunningEodBean;
    }

    public List<ProcessSummeryBean> getEodProcessSummeryList(Long eodID) {
        List<ProcessSummeryBean> processSummeryList = new ArrayList<>();
        try {
            processSummeryList = processSummeryRepo.findProcessSummeryListById(eodID);
        } catch (Exception e) {
            throw e;
        }
        return processSummeryList;
    }

    public List<Object> getEodInvalidTransactionList(Long eodId) {
        List<Object> invalidTransactionBeanList = new ArrayList<>();

        try {
            List<RECATMFILEINVALID> recAtmFileInvalidList = atmFileInvalidRepo.findRECATMFILEINVALIDByEODID(eodId);
            List<RECPAYMENTFILEINVALID> recPaymentFileInvalidList = paymentFileInvalidRepo.findRECPAYMENTFILEINVALIDByEODID(eodId);

            recAtmFileInvalidList.forEach(eod -> {
                EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                eodInvalidTransactionBean.setEodId(eod.getEODID());
                eodInvalidTransactionBean.setFileId(eod.getFILEID());
                eodInvalidTransactionBean.setFileType("ATM");
                eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                invalidTransactionBeanList.add(eodInvalidTransactionBean);
            });

            recPaymentFileInvalidList.forEach(eod -> {
                EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                eodInvalidTransactionBean.setEodId(eod.getEODID());
                eodInvalidTransactionBean.setFileId(eod.getFILEID());
                eodInvalidTransactionBean.setFileType("PAYMENT");
                eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                invalidTransactionBeanList.add(eodInvalidTransactionBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return invalidTransactionBeanList;
    }

    public List<EodErrorMerchantBean> getEodErrorMerchantList(Long eodId) {
        List<EodErrorMerchantBean> eodErrorMerchantBeansList = new ArrayList<>();

        try {
            List<EODERRORMERCHANT> eodErrorMerchantList = eodErrorMerchantListRepo.findEODERRORMERCHANTByEODID(eodId);

            eodErrorMerchantList.forEach(eod -> {
                EodErrorMerchantBean errorMerchantBean = new EodErrorMerchantBean();
                errorMerchantBean.setEodId(eod.getEODID());
                errorMerchantBean.setMerchantId(eod.getMID());
                errorMerchantBean.setErrorProcessId(eod.getERRORPROCESSID());
                errorMerchantBean.setErrorReason(eod.getERRORREMARK());

                eodErrorMerchantBeansList.add(errorMerchantBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return eodErrorMerchantBeansList;
    }

    public List<EodErrorCardBean> getEodErrorCardList(Long eodId) {
        List<EodErrorCardBean> errorCardBeanList = new ArrayList<>();
        try {
            List<EODERRORCARDS> eodErrorCardsList = eodErrorCardListRepo.findEODERRORCARDSByEODID(eodId);

            eodErrorCardsList.forEach(eod -> {
                EodErrorCardBean errorCardBean = new EodErrorCardBean();
                errorCardBean.setEodId(eod.getEODID());
                //errorCardBean.setCardNumber(CommonMethods.cardNumberMask(eod.getCARDNO()));
                errorCardBean.setCardNumber(eod.getCARDNO());
                errorCardBean.setErrorProcess(eod.getERRORPROCESSID());
                errorCardBean.setErrorReason(eod.getERRORREMARK());

                errorCardBeanList.add(errorCardBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return errorCardBeanList;
    }
}
