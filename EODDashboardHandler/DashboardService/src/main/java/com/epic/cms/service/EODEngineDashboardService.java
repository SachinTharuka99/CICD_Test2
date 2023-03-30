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
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    @Autowired
    EodProcessFlowRepo eodProcessFlowRepo;

    @Autowired
    private ModelMapper modelMapper;

    public EodBean getEodInfoList(Long eodId) {
        EodBean eodBean = new EodBean();
        try {
            Optional<EOD> eodInfo = eodIdInfoRepo.findById(eodId);

            int count1 = eodProcessFlowRepo.countByPROCESSCATEGORYIDNotIn(Collections.singletonList(90));

            eodInfo.ifPresent(eod -> {
                eodBean.setEodId(eod.getEODID());
                eodBean.setStartTime(eod.getSTARTTIME());
                eodBean.setEndTime(eod.getENDTIME());
                eodBean.setStatus(eod.getSTATUS().getSTATUSCODE());
                eodBean.setSubEodStatus(eod.getSUBEODSTATUS());
                eodBean.setNoOfSuccessProcess(eod.getNOOFSUCCESSPROCESS());
                eodBean.setNoOfErrorProcess(eod.getNOOFERRORPAROCESS());
                eodBean.setTotalProcessCount(count1);
            });
        } catch (Exception e) {
            throw e;
        }
        return eodBean;
    }

    public NextRunningEodBean getNextRunningEodId() {
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
            processSummeryList = processSummeryRepo.findProcessSummeryListById(eodID, Configurations.EOD_ENGINE);
        } catch (Exception e) {
            throw e;
        }
        return processSummeryList;
    }

    public List<Object> getEodInvalidTransactionList(RequestBean requestBean, Long eodId) {
        List<Object> invalidTransactionBeanList = new ArrayList<>();
        try {
            int fixSize = requestBean.getSize() / 2;
            Pageable paging = PageRequest.of(requestBean.getPage(), fixSize, Sort.by("FILEID").ascending());

            List<RECATMFILEINVALID> recAtmFileInvalidList = atmFileInvalidRepo.findRECATMFILEINVALIDByEODID(eodId, paging);
            List<RECPAYMENTFILEINVALID> recPaymentFileInvalidList = paymentFileInvalidRepo.findRECPAYMENTFILEINVALIDByEODID(eodId, paging);

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

    public List<EodErrorCardBean> getEodErrorCardList(RequestBean requestBean, Long eodId) {
        List<EodErrorCardBean> eodErrorCardBeans = new ArrayList<>();
        try {
            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

            List<EODERRORCARDS> eoderrorcardsByEODID = eodErrorCardListRepo.findEODERRORCARDSByEODID(eodId, paging);

            eoderrorcardsByEODID.forEach(eod -> {
                EodErrorCardBean bean = new EodErrorCardBean();
                bean.setEodId(eod.getEODID());
                bean.setCardNumber(eod.getCARDNO());
                bean.setErrorProcess(eod.getERRORPROCESSID());
                bean.setErrorReason(eod.getERRORREMARK());
                eodErrorCardBeans.add(bean);
            });

        } catch (Exception e) {
            throw e;
        }
        return eodErrorCardBeans;
    }

    public List<EodErrorMerchantBean> getEodErrorMerchantList(RequestBean requestBean, Long eodId) {
        List<EodErrorMerchantBean> errorMerchantBeans = new ArrayList<>();
        try {
            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

            List<EODERRORMERCHANT> eoderrormerchantByEODID = eodErrorMerchantListRepo.findEODERRORMERCHANTByEODID(eodId, paging);

            eoderrormerchantByEODID.forEach(eod -> {
                EodErrorMerchantBean bean = new EodErrorMerchantBean();
                bean.setEodId(eod.getEODID());
                bean.setMerchantId(eod.getMID());
                bean.setErrorProcessId(eod.getERRORPROCESSID());
                bean.setErrorReason(eod.getERRORREMARK());
                errorMerchantBeans.add(bean);
            });
        } catch (Exception e) {
            throw e;
        }
        return errorMerchantBeans;
    }
}
