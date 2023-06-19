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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
    EodProcessFlowRepo eodProcessFlowRepo;

    @Autowired
    EodOutputFileRepo eodOutputFileRepo;

    @Autowired
    StatementGenSummeryListRepo genSummeryListRepo;

    @Autowired
    EodAtmInputFileRepo eodAtmInputFileRepo;

    @Autowired
    EodMasterInputFileRepo eodMasterInputFileRepo;

    @Autowired
    EodVisaInputFileRepo eodVisaInputFileRepo;

    @Autowired
    EodPaymentInputFileRepo eodPaymentInputFileRepo;

    @Autowired
    FileProcessingSummeryListRepo processingSummeryListRepo;


    public Long dashboardCurrentEodId = 0L;

    public EodBean getEodInfoList(Long eodId) {
        EodBean eodBean = new EodBean();
        try {
            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }
            Optional<EOD> eodInfo = eodIdInfoRepo.findById(eodId);

            int count1 = eodProcessFlowRepo.countByPROCESSCATEGORYIDNotIn(Collections.singletonList(90));

            eodInfo.ifPresent(eod -> {
                eodBean.setEodId(eod.getEODID());
                eodBean.setStartTime(eod.getSTARTTIME());
                eodBean.setEndTime(eod.getENDTIME());
                eodBean.setStatus(eod.getSTATUS());
                eodBean.setFileGenStatus(eod.getFILEGENERATIONSTATUS());
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

    public void getCurrentDashboardEodId () {
        int count = 0;
        Long eodId = 0L;
        Long currentEodId = 0L;

        try {
            List<EOD> byEODIDDesc = eodIdInfoRepo.findAllByOrderByEODIDDesc();

            for (EOD eod:byEODIDDesc) {
                count++;
                eodId = eod.getEODID();
                String status = eod.getSTATUS();

                if (status.equals("INPR")) {
                    currentEodId = eodId;
                    break;
                } else {
                    if (count == 2) { // get the privious finished eod id..
                        currentEodId = eodId;
                        break;
                    }
                }

            }
           // eodInfoList = getEodInfoList(currentEodId);

            dashboardCurrentEodId = currentEodId;
        } catch (Exception e) {
            throw e;
        }
        //return eodInfoList;
    }

    public List<ProcessSummeryBean> getEodProcessSummeryList(Long eodID) {
        List<ProcessSummeryBean> processSummeryList = new ArrayList<>();
        try {

            if (eodID == 0) {
                eodID = dashboardCurrentEodId;
            }

            processSummeryList = processSummeryRepo.findProcessSummeryListById(eodID, Configurations.EOD_ENGINE);
        } catch (Exception e) {
            throw e;
        }
        return processSummeryList;
    }

    public DataTableBean getEodInvalidTransactionList(RequestBean requestBean, Long eodId) {
        List<Object> invalidTransactionBeanList = new ArrayList<>();
        DataTableBean dataTableBean = new DataTableBean();
        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            int fixSize = requestBean.getSize() / 2;
            Pageable paging = PageRequest.of(requestBean.getPage(), fixSize, Sort.by("FILEID").ascending());
            //Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize(), Sort.by("FILEID").ascending());

            Page<RECATMFILEINVALID> recAtmFileInvalidList = atmFileInvalidRepo.findRECATMFILEINVALIDByEODID(eodId, paging);
            Page<RECPAYMENTFILEINVALID> recPaymentFileInvalidList = paymentFileInvalidRepo.findRECPAYMENTFILEINVALIDByEODID(eodId, paging);

            int content = recAtmFileInvalidList.getContent().size() + recPaymentFileInvalidList.getContent().size();

            if (content > 0) {
                dataTableBean.setCount(recAtmFileInvalidList.getTotalElements() + recPaymentFileInvalidList.getTotalElements());
                dataTableBean.setPagecount(recAtmFileInvalidList.getTotalPages() + recPaymentFileInvalidList.getTotalPages());

                recAtmFileInvalidList.getContent().forEach(eod -> {
                    EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                    eodInvalidTransactionBean.setEodId(eod.getEODID());
                    eodInvalidTransactionBean.setFileId(eod.getFILEID());
                    eodInvalidTransactionBean.setFileType("ATM");
                    eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                    eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                    invalidTransactionBeanList.add(eodInvalidTransactionBean);
                });

                recPaymentFileInvalidList.getContent().forEach(eod -> {
                    EodInvalidTransactionBean eodInvalidTransactionBean = new EodInvalidTransactionBean();
                    eodInvalidTransactionBean.setEodId(eod.getEODID());
                    eodInvalidTransactionBean.setFileId(eod.getFILEID());
                    eodInvalidTransactionBean.setFileType("PAYMENT");
                    eodInvalidTransactionBean.setLineNumber(eod.getLINENUMBER());
                    eodInvalidTransactionBean.setErrorRemark(eod.getERRORDESC());

                    invalidTransactionBeanList.add(eodInvalidTransactionBean);
                });
                dataTableBean.setList(invalidTransactionBeanList);
            } else {
                dataTableBean.setCount(recAtmFileInvalidList.getTotalElements() + recPaymentFileInvalidList.getTotalElements());
                dataTableBean.setPagecount(recAtmFileInvalidList.getTotalPages() + recPaymentFileInvalidList.getTotalPages());
                dataTableBean.setList(invalidTransactionBeanList);
            }
        } catch (Exception e) {
            throw e;
        }
        return dataTableBean;
    }

    public DataTableBean getEodErrorCardList(RequestBean requestBean, Long eodId) {
        List<Object> eodErrorCardBeans = new ArrayList<>();
        DataTableBean dataTableBean = new DataTableBean();

        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

            Page<EODERRORCARDS> eoderrorcards = eodErrorCardListRepo.findAllByEODID(eodId, paging);

            if (!eoderrorcards.getContent().isEmpty()) {
                dataTableBean.setCount(eoderrorcards.getTotalElements());
                dataTableBean.setPagecount(eoderrorcards.getTotalPages());

                eoderrorcards.getContent().forEach(eod -> {
                    EodErrorCardBean bean = new EodErrorCardBean();
                    bean.setEodId(eod.getEODID());
                    bean.setCardNumber(eod.getCARDNO());
                    bean.setErrorProcess(eod.getERRORPROCESSID());
                    bean.setErrorReason(eod.getERRORREMARK());
                    eodErrorCardBeans.add(bean);
                });
                dataTableBean.setList(eodErrorCardBeans);
            } else {
                dataTableBean.setCount(eoderrorcards.getTotalElements());
                dataTableBean.setPagecount(eoderrorcards.getTotalPages());
                dataTableBean.setList(eodErrorCardBeans);
            }
        } catch (Exception e) {
            throw e;
        }
        return dataTableBean;
    }

    public DataTableBean getEodErrorMerchantList(RequestBean requestBean, Long eodId) {
        List<Object> errorMerchantBeans = new ArrayList<>();
        DataTableBean dataTableBean = new DataTableBean();
        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            Pageable paging = PageRequest.of(requestBean.getPage(), requestBean.getSize());

            Page<EODERRORMERCHANT> eoderrormerchant = eodErrorMerchantListRepo.findEODERRORMERCHANTByEODID(eodId, paging);

            if (!eoderrormerchant.getContent().isEmpty()) {
                dataTableBean.setCount(eoderrormerchant.getTotalElements());
                dataTableBean.setPagecount(eoderrormerchant.getTotalPages());

                eoderrormerchant.getContent().forEach(eod -> {
                    EodErrorMerchantBean bean = new EodErrorMerchantBean();
                    bean.setEodId(eod.getEODID());
                    bean.setMerchantId(eod.getMID());
                    bean.setErrorProcessId(eod.getERRORPROCESSID());
                    bean.setErrorReason(eod.getERRORREMARK());
                    errorMerchantBeans.add(bean);
                });

                dataTableBean.setList(errorMerchantBeans);
            } else {
                dataTableBean.setCount(eoderrormerchant.getTotalElements());
                dataTableBean.setPagecount(eoderrormerchant.getTotalPages());
                dataTableBean.setList(errorMerchantBeans);
            }

        } catch (Exception e) {
            throw e;
        }
        return dataTableBean;
    }

    //File Generation
    public List<EodOutputFileBean> getEodOutputFIleList(Long eodId) {
        List<EodOutputFileBean> outputFileBeanList = new ArrayList<>();

        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            List<Object[]> eodOutputFilesList = eodOutputFileRepo.findEODOUTPUTFILESByEODID(eodId);

            Long finalEodId = eodId;
            eodOutputFilesList.forEach(eod -> {
                EodOutputFileBean eodBean = new EodOutputFileBean();
                eodBean.setEodId(finalEodId);
                eodBean.setCreatedTime((Date) eod[0]);
                eodBean.setFileType((String) eod[1]);
                eodBean.setNoOfRecords((int) ((BigDecimal) eod[3]).doubleValue());
                eodBean.setFileName((String) eod[4]);
                eodBean.setSubFolder((String) eod[5]);

                outputFileBeanList.add(eodBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return outputFileBeanList;
    }

    public List<StatementGenSummeryBean> getStatementGenSummeryList(Long eodId) {
        List<StatementGenSummeryBean> stmtGenSummeryList = new ArrayList<>();

        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            stmtGenSummeryList = genSummeryListRepo.findStmtGenSummeryListByEodId(eodId, Configurations.EOD_FILE_GENERATION);
        } catch (Exception e) {
            throw e;
        }
        return stmtGenSummeryList;
    }

    //File Processing
    public List<Object> getEodInputFIleList(Long eodId) {
        List<Object> eodInputFileObjectList = new ArrayList<>();

        try {

            if (eodId == 0) {
                eodId = dashboardCurrentEodId;
            }

            List<EODATMFILE> atmInputFileList = eodAtmInputFileRepo.findEODATMFILEByEODID(eodId);
            List<EODPAYMENTFILE> paymentInputFileList = eodPaymentInputFileRepo.findEODPAYMENTFILEByEODID(eodId);
            List<EODMASTERFILE> masterInputFileList = eodMasterInputFileRepo.findEODMASTERFILEByEODID(eodId);
            List<EODVISAFILE> visaInputFileList = eodVisaInputFileRepo.findEODVISAFILEByEODID(eodId);


            atmInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("ATM");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            paymentInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("PAYMENT");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            masterInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("VISA");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });

            visaInputFileList.forEach(eod -> {
                EodInputFileBean inputFileBean = new EodInputFileBean();
                inputFileBean.setUploadTime(eod.getUPLOADTIME());
                inputFileBean.setFileType("MASTER");
                inputFileBean.setFileId(eod.getFILEID());
                inputFileBean.setFileName(eod.getFILENAME());
                inputFileBean.setStatus(eod.getSTATUS());

                eodInputFileObjectList.add(inputFileBean);
            });
        } catch (Exception e) {
            throw e;
        }
        return eodInputFileObjectList;
    }

    public List<StatementGenSummeryBean> getProcessingSummeryList(Long eodID) {
        List<StatementGenSummeryBean> processingSummeryBeans = new ArrayList<>();
        try {

            if (eodID == 0) {
                eodID = dashboardCurrentEodId;
            }

            processingSummeryBeans = processingSummeryListRepo.findProcessingSummeryListByEodId(eodID, Configurations.EOD_FILE_PROCESSING);
        } catch (Exception e) {
            throw e;
        }
        return processingSummeryBeans;
    }

}
