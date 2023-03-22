/**
 * Author : rasintha_j
 * Date : 3/18/2023
 * Time : 6:40 AM
 * Project Name : ecms_eod_product
 */

package com.epic.cms.service;

import com.epic.cms.common.ComSpecification;
import com.epic.cms.common.Common;
import com.epic.cms.model.bean.*;
import com.epic.cms.model.entity.*;
import com.epic.cms.repository.*;
import com.epic.cms.util.Configurations;
import com.epic.cms.util.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.epic.cms.common.Common.searchAuditString;


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
    Common common;

    @Autowired
    ComSpecification comSpecification;

    @Autowired
    private ModelMapper modelMapper;

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
            processSummeryList = processSummeryRepo.findProcessSummeryListById(eodID, Configurations.EOD_ENGINE);
        } catch (Exception e) {
            throw e;
        }
        return processSummeryList;
    }

//    public List<Object> getEodInvalidTransactionList(Long eodId) {
//        List<Object> invalidTransactionBeanList = new ArrayList<>();
//
//        try {
//            List<RECATMFILEINVALID> recAtmFileInvalidList = atmFileInvalidRepo.findRECATMFILEINVALIDByEODID(eodId);
//            List<RECPAYMENTFILEINVALID> recPaymentFileInvalidList = paymentFileInvalidRepo.findRECPAYMENTFILEINVALIDByEODID(eodId);
//
//            recAtmFileInvalidList.forEach(eod -> {
//                EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
//                eodInvalidTransactionBean.setEodId(eod.getEODID());
//                eodInvalidTransactionBean.setFileId(eod.getFILEID());
//                eodInvalidTransactionBean.setFileType("ATM");
//                eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
//                eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());
//
//                invalidTransactionBeanList.add(eodInvalidTransactionBean);
//            });
//
//            recPaymentFileInvalidList.forEach(eod -> {
//                EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
//                eodInvalidTransactionBean.setEodId(eod.getEODID());
//                eodInvalidTransactionBean.setFileId(eod.getFILEID());
//                eodInvalidTransactionBean.setFileType("PAYMENT");
//                eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
//                eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());
//
//                invalidTransactionBeanList.add(eodInvalidTransactionBean);
//            });
//        } catch (Exception e) {
//            throw e;
//        }
//        return invalidTransactionBeanList;
//    }

    public DataTableBean getEodInvalidTransactionList(RequestBean requestBean, Long eodId) {
        DataTableBean dataTableBean = new DataTableBean();
        List<Object> generalLedgerMgtDataBeans = new ArrayList<>();
        Specification<Object> specification = null;

        if (requestBean.isSearch() && requestBean.getRequestBody() != null) {
            EodInvalidTransactionBean transactionBean = modelMapper.map(requestBean.getRequestBody(), EodInvalidTransactionBean.class);

            //Set audit description
            String description = "";
            description = searchAuditString(description, "Eod Id", transactionBean.getEodId().toString());
            description = searchAuditString(description, "Invalid Transaction File Id", transactionBean.getFileId());
            description = searchAuditString(description, "Invalid Transaction File Type", transactionBean.getFileType());
            description = searchAuditString(description, "Line Number", String.valueOf(transactionBean.getLineNumber()));
            description = searchAuditString(description, "Error Response", transactionBean.getErrorRemark());

            specification = comSpecification.makeInvalidTransactionSpecification(transactionBean);
        }
        //List<Sort.Order> orders = common.getSort(requestBean.getSort());

        Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

        Page<RECATMFILEINVALID> recatmfileinvalidByEODID = atmFileInvalidRepo.findRECATMFILEINVALIDByEODID(eodId, paging);

        if (recatmfileinvalidByEODID != null) {
            dataTableBean.setCount(recatmfileinvalidByEODID.getTotalElements());
            dataTableBean.setPagecount(recatmfileinvalidByEODID.getTotalPages());
        }

        recatmfileinvalidByEODID.forEach(eod -> {
            EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
            eodInvalidTransactionBean.setEodId(eod.getEODID());
            eodInvalidTransactionBean.setFileId(eod.getFILEID());
            eodInvalidTransactionBean.setFileType("ATM");
            eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
            eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

            generalLedgerMgtDataBeans.add(eodInvalidTransactionBean);
        });

        Page<RECPAYMENTFILEINVALID> recpaymentfileinvalidByEODID = paymentFileInvalidRepo.findRECPAYMENTFILEINVALIDByEODID(eodId, paging);

        if (recpaymentfileinvalidByEODID != null) {
            dataTableBean.setCount(recpaymentfileinvalidByEODID.getTotalElements());
            dataTableBean.setPagecount(recpaymentfileinvalidByEODID.getTotalPages());
        }

        recpaymentfileinvalidByEODID.forEach(eod -> {
            EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
            eodInvalidTransactionBean.setEodId(eod.getEODID());
            eodInvalidTransactionBean.setFileId(eod.getFILEID());
            eodInvalidTransactionBean.setFileType("PAYMENT");
            eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
            eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

            generalLedgerMgtDataBeans.add(eodInvalidTransactionBean);
        });

        dataTableBean.setList(generalLedgerMgtDataBeans);
        return dataTableBean;
    }

    public DataTableBean getEodErrorCardList(RequestBean requestBean, Long eodId) {
        DataTableBean dataTableBean = new DataTableBean();
        List<Object> generalLedgerMgtDataBeans = new ArrayList<>();
        Page<EODERRORCARDS> eoderrorcardsPage;
        Specification<EODERRORCARDS> specification = null;

        if (requestBean.isSearch() && requestBean.getRequestBody() != null) {
            EodErrorCardBean errorCardBean = modelMapper.map(requestBean.getRequestBody(), EodErrorCardBean.class);

            //Set audit description
            String description = "";
            description = searchAuditString(description, "Eod Id", errorCardBean.getEodId().toString());
            description = searchAuditString(description, "Error Card Number", errorCardBean.getCardNumber());
            description = searchAuditString(description, "Error Process Id", errorCardBean.getErrorProcess());
            description = searchAuditString(description, "Error Response", errorCardBean.getErrorReason());

            specification = comSpecification.makeErrorCardSpecification(errorCardBean);

        }

        //List<Sort.Order> orders = common.getSort(requestBean.getSort());

        Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

        Page<EODERRORCARDS> eoderrorcardsByEODID = eodErrorCardListRepo.findEODERRORCARDSByEODID(eodId, paging);

        if (eoderrorcardsByEODID != null) {
            dataTableBean.setCount(eoderrorcardsByEODID.getTotalElements());
            dataTableBean.setPagecount(eoderrorcardsByEODID.getTotalPages());
        }

        eoderrorcardsByEODID.forEach(eod -> {
            EodErrorCardBean bean = new EodErrorCardBean();
            bean.setEodId(eod.getEODID());
            bean.setCardNumber(eod.getCARDNO());
            bean.setErrorProcess(eod.getERRORPROCESSID());
            bean.setErrorReason(eod.getERRORREMARK());
            generalLedgerMgtDataBeans.add(bean);
        });

        dataTableBean.setList(generalLedgerMgtDataBeans);
        return dataTableBean;
    }

    public DataTableBean getEodErrorMerchantList(RequestBean requestBean, Long eodId) {
        DataTableBean dataTableBean = new DataTableBean();
        List<Object> generalLedgerMgtDataBeans = new ArrayList<>();
        Page<EODERRORMERCHANT> eodErrorMerchant;
        Specification<EODERRORMERCHANT> specification = null;

        if (requestBean.isSearch() && requestBean.getRequestBody() != null) {
            EodErrorMerchantBean errorMerchantBean = modelMapper.map(requestBean.getRequestBody(), EodErrorMerchantBean.class);

            //Set audit description
            String description = "";
            description = searchAuditString(description, "Merchant Id", errorMerchantBean.getMerchantId());
            description = searchAuditString(description, "Error Response", errorMerchantBean.getErrorReason());
            description = searchAuditString(description, "Error Process Id", errorMerchantBean.getErrorProcessId());
            description = searchAuditString(description, "EodId", errorMerchantBean.getEodId().toString());

            specification = comSpecification.makeMerchantSpecification(errorMerchantBean);

        }

        //List<Sort.Order> orders = common.getSort(requestBean.getSort());

        Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

        Page<EODERRORMERCHANT> eoderrormerchantByEODID = eodErrorMerchantListRepo.findEODERRORMERCHANTByEODID(eodId, paging);

        if (eoderrormerchantByEODID != null) {
            dataTableBean.setCount(eoderrormerchantByEODID.getTotalElements());
            dataTableBean.setPagecount(eoderrormerchantByEODID.getTotalPages());
        }

        eoderrormerchantByEODID.forEach(eod -> {
            EodErrorMerchantBean bean = new EodErrorMerchantBean();
            bean.setEodId(eod.getEODID());
            bean.setMerchantId(eod.getMID());
            bean.setErrorProcessId(eod.getERRORPROCESSID());
            bean.setErrorReason(eod.getERRORREMARK());
            generalLedgerMgtDataBeans.add(bean);
        });

        dataTableBean.setList(generalLedgerMgtDataBeans);
        return dataTableBean;
    }
}
